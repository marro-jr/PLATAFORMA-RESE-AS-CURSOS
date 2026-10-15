# Plataforma de Reseñas de Cursos

Aplicación web desarrollada bajo un estándar preprofesional sólido de Ingeniería de Software que permite a los estudiantes consultar un catálogo de cursos, publicar reseñas y valoraciones, y someter dichas opiniones a un flujo de moderación antes de que se hagan públicas.

---

## 1. Stack Tecnológico

### Backend
- **Lenguaje:** Java 21 (LTS).
- **Framework:** Spring Boot 3.4.4.
- **Módulos:** Spring Web, Spring Data JPA, Spring Security, Bean Validation.
- **Seguridad:** JSON Web Tokens (JJWT 0.12.6) con algoritmo HMAC SHA-256 y contraseñas hasheadas con BCrypt.
- **Persistencia:** Hibernate ORM, MySQL Connector/J y base de datos H2 en memoria para pruebas e inicio inmediato sin dependencias de infraestructura.
- **Documentación API:** SpringDoc OpenAPI 3 / Swagger UI 2.8.x.
- **Pruebas:** JUnit 5, Mockito y Spring Boot MockMvc.
- **Gestor de Construcción:** Apache Maven (mediante Maven Wrapper `mvnw`).

### Frontend
- **Librería UI:** React 18 con TypeScript en modo estricto.
- **Herramienta de Construcción:** Vite.
- **Estilos:** Bootstrap 5.
- **Comunicación HTTP:** Fetch API nativa.
- **Seguridad en Cliente:** Token JWT almacenado estrictamente en memoria de la aplicación (sin almacenamiento inseguro en `localStorage`).

### Base de Datos
- **Motor:** MySQL 8.0.
- **Scripts:** Esquema DDL en `database/schema.sql` y datos ficticios reproducibles en `database/data.sql`.

---

## 2. Arquitectura del Sistema

El proyecto implementa una arquitectura por capas estricta y desacoplada:

```
[React + TypeScript] (Frontend)
         ↓  HTTP / REST JSON (Bearer JWT)
[Controller] (Recibe peticiones, valida DTOs mediante Bean Validation y delega)
         ↓  Inyección de Dependencias por Constructor
[Service] (Contiene lógica de negocio, validación de reglas de dominio y permisos)
         ↓  Inyección de Dependencias por Constructor
[Repository] (Acceso a datos con Spring Data JPA y consultas derivadas)
         ↓  JDBC
[Base de Datos: MySQL / H2]
```

### Estructura de Paquetes en Backend
- `com.plataforma.resenas.controller`: Endpoints REST con anotaciones OpenAPI.
- `com.plataforma.resenas.service`: Lógica de negocio y reglas de dominio.
- `com.plataforma.resenas.repository`: Interfaces JPA con métodos derivados de consulta.
- `com.plataforma.resenas.entity`: Entidades mapeadas (`AppUser`, `Course`, `Review`, `Role`, `ReviewStatus`).
- `com.plataforma.resenas.dto`: Records inmutables para peticiones y respuestas HTTP.
- `com.plataforma.resenas.security`: Configuración de Spring Security, `JwtService`, `JwtAuthFilter` y `CustomUserDetailsService`.
- `com.plataforma.resenas.exception`: Excepciones de dominio y manejador global `@RestControllerAdvice`.
- `com.plataforma.resenas.config`: Configuración de Swagger y cargador de datos iniciales (`DataInitializer`).

---

## 3. Modelo de Entidades y Restricciones

### AppUser
- `id`: Identificador numérico autoincremental.
- `name`: Nombre completo del usuario.
- `email`: Correo electrónico único en la plataforma.
- `passwordHash`: Hash BCrypt de la contraseña.
- `role`: Rol del usuario (`USER`, `MODERATOR`).
- `active`: Estado booleano de la cuenta.

### Course
- `id`: Identificador numérico.
- `title`: Título del curso.
- `description`: Descripción del contenido.
- `category`: Categoría temática (Backend, Frontend, DevOps, etc.).
- `active`: Estado del curso.

### Review
- `id`: Identificador numérico.
- `user`: Relación `@ManyToOne` con `AppUser`.
- `course`: Relación `@ManyToOne` con `Course`.
- `rating`: Entero entre 1 y 5.
- `comment`: Texto de la opinión (obligatorio, hasta 1500 caracteres).
- `status`: Estado de moderación (`PENDING`, `APPROVED`, `REJECTED`).
- `createdAt` / `updatedAt`: Marcas de tiempo de creación y última actualización.

> **Restricción de Unicidad:** Se aplica una restricción única `uk_user_course` sobre `(user_id, course_id)` a nivel de base de datos y validación en `ReviewService`, impidiendo que un mismo usuario registre más de una reseña por curso.

---

## 4. Flujo de Autenticación y Autorización (Spring Security + JWT)

1. **Registro:** El usuario envía su nombre, correo y contraseña a `POST /api/auth/register`. El servicio verifica la unicidad del correo, aplica hash BCrypt y **fuerza siempre el rol USER**, ignorando cualquier parámetro que intente atribuirse privilegios de moderador.
2. **Inicio de Sesión:** El usuario envía credenciales a `POST /api/auth/login`. `AuthenticationManager` valida contra `CustomUserDetailsService` y `BCryptPasswordEncoder`.
3. **Generación de Token:** `JwtService` genera un token firmado con HMAC SHA-256 conteniendo `sub` (correo), `rol` y `nombre`, con expiración configurable.
4. **Almacenamiento en Cliente:** React almacena el token únicamente en la memoria de la sesión (React Context). No se persiste en `localStorage` para prevenir ataques de secuestro de token.
5. **Peticiones Protegidas:** React envía la cabecera `Authorization: Bearer <token>`.
6. **Intercepción y Validación:** `JwtAuthFilter` (que extiende `OncePerRequestFilter`) extrae el token, verifica la firma, comprueba que no esté expirado, valida que el usuario continúe activo en la base de datos y establece la autenticación en el `SecurityContextHolder`.
7. **Control de Acceso:** Las rutas de moderación (`/api/moderation/**`) exigen `hasRole('MODERATOR')`. Las rutas de publicación y consulta propia (`/api/courses/*/reviews`, `/api/me/reviews`, `/api/reviews/*`) exigen `hasRole('USER')`.

---

## 5. Reglas de Negocio Implementadas

- **Estado inicial:** Toda reseña nueva nace en estado `PENDING`.
- **Rango de puntuación:** Calificación obligatoria entre 1 y 5 estrellas.
- **Comentario obligatorio:** No se admiten reseñas en blanco o únicamente con espacios.
- **Cursos activos:** No es posible publicar reseñas en cursos marcados como inactivos.
- **Visibilidad pública:** El endpoint público `GET /api/courses/{id}/reviews` retorna estrictamente reseñas en estado `APPROVED`.
- **Propiedad de la reseña:** Solo el usuario autor puede editar su propia reseña. Si otro usuario lo intenta, se retorna `403 Forbidden`.
- **Inmutabilidad de reseñas aprobadas:** Una reseña en estado `APPROVED` no puede ser modificada en esta versión (retorna `400 Bad Request`).
- **Reevaluación de reseñas rechazadas:** Si el usuario edita una reseña en estado `REJECTED`, el backend actualiza el contenido y regresa automáticamente su estado a `PENDING` para una nueva evaluación.
- **Exclusividad de moderación:** Solo usuarios con rol `MODERATOR` pueden aprobar o rechazar reseñas.
- **Moderación única:** No se permite aprobar o rechazar reseñas que ya fueron moderadas previamente (deben encontrarse en `PENDING`).

---

## 6. Endpoints de la API REST

| Método | Endpoint | Acceso | Descripción |
|---|---|---|---|
| POST | `/api/auth/register` | Público | Registro de estudiante (rol forzado USER) |
| POST | `/api/auth/login` | Público | Autenticación y obtención de Bearer JWT |
| GET | `/api/courses` | Público | Listar catálogo de cursos activos con promedio |
| GET | `/api/courses/{id}` | Público | Detalle de un curso específico |
| GET | `/api/courses/{id}/reviews` | Público | Listar únicamente reseñas APPROVED del curso |
| POST | `/api/courses/{id}/reviews` | USER | Publicar reseña (inicia en estado PENDING) |
| GET | `/api/me/reviews` | USER | Consultar reseñas publicadas por el usuario actual |
| PATCH | `/api/reviews/{id}` | USER | Editar reseña propia en estado PENDING o REJECTED |
| GET | `/api/moderation/reviews` | MODERATOR | Listar reseñas para moderación (filtro opcional por estado) |
| PATCH | `/api/moderation/reviews/{id}/status` | MODERATOR | Aprobar o rechazar reseña en PENDING |

Documentación interactiva disponible en Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## 7. Cuentas de Demostración Preconfiguradas

Contraseña para todas las cuentas de prueba: `Password123!`

| Nombre | Correo | Rol | Propósito |
|---|---|---|---|
| Moderador Principal | `moderador@correo.com` | `MODERATOR` | Acceso al panel de moderación para aprobar o rechazar |
| Juan Pérez | `juan.perez@correo.com` | `USER` | Estudiante con reseñas aprobadas existentes |
| María García | `maria.garcia@correo.com` | `USER` | Estudiante con reseña pendiente de moderación |
| Ana Torres | `ana.torres@correo.com` | `USER` | Estudiante con reseña rechazada para probar edición |

---

## 8. Instrucciones de Ejecución

### Prerrequisitos
- Java 21 o superior instalado.
- Node.js (v18+) y npm instalados.
- MySQL 8.0 (opcional para el perfil mysql; por defecto el proyecto incluye perfil h2 listo para ejecución inmediata).

### Configuración de Variables de Entorno

El sistema no utiliza claves ni credenciales sensibles hardcodeadas en el código fuente ni en los archivos de propiedades. Todas las variables se configuran a través del entorno o del archivo `.env`:

1. Copiar la plantilla de configuración:
   ```bash
   cp .env.example .env
   # o en Windows PowerShell:
   Copy-Item .env.example .env
   ```
2. Variables principales configuradas en `.env`:
   - `JWT_SECRET_BASE64`: Clave Base64 obligatoria para la firma de tokens JWT (mínimo 256 bits).
   - `DB_USERNAME` y `DB_PASSWORD`: Credenciales de MySQL (sin valores inseguros quemados en el código).
   - `SPRING_PROFILES_ACTIVE`: Perfil activo (`h2` para entorno autocontenido o `mysql` para servidor relacional).

### Ejecución del Backend

1. Acceder al directorio `backend`:
   ```bash
   cd backend
   ```
2. Ejecutar los tests automatizados:
   ```bash
   .\mvnw.cmd test
   ```
3. Iniciar el servidor Spring Boot (perfil H2 en memoria por defecto):
   ```bash
   .\mvnw.cmd spring-boot:run
   ```
   *Para ejecutar con MySQL 8.0 local:*
   - Ejecutar los scripts `database/schema.sql` y `database/data.sql` en su servidor MySQL.
   - Iniciar con la variable de entorno:
     ```bash
     $env:SPRING_PROFILES_ACTIVE="mysql"; .\mvnw.cmd spring-boot:run
     ```
   El backend estará disponible en `http://localhost:8080`.

### Ejecución del Frontend

1. Acceder al directorio `frontend`:
   ```bash
   cd frontend
   ```
2. Instalar dependencias:
   ```bash
   npm install
   ```
3. Iniciar el servidor de desarrollo Vite:
   ```bash
   npm run dev
   ```
   La aplicación web estará disponible en `http://localhost:5173`.

---

## 9. Pruebas Automatizadas

El proyecto incluye 19 pruebas unitarias y de integración que validan los requisitos críticos:

- `registrarUsuarioValido_debeRetornar201YRolUser`
- `registrarUsuarioConIntentoModerador_debeForzarRolUser`
- `loginValido_debeRetornarTokenJWT`
- `loginIncorrecto_debeRetornar401`
- `jwtAusente_debeRetornar401`
- `jwtAlterado_debeRetornar401`
- `soloResenasApprovedAparecenPublicamente`
- `userIntentaAprobarResena_debeRetornar403`
- `moderatorApruebaResena_debeRetornar200`
- `noPermiteModerarResenasYaModeradas`
- `userIntentaEditarResenaAjena_debeRetornar403`
- `resenaApproved_noPuedeModificarse`
- `resenaDuplicada_debeRetornar409`
- `resenaRechazadaEditada_regresaAPending`
- Pruebas unitarias de aislamiento con Mockito sobre `ReviewService`.
