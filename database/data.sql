-- =============================================================================
-- PROYECTO: Plataforma de Reseñas de Cursos
-- ARCHIVO: database/data.sql
-- DESCRIPCION: Datos ficticios reproducibles para pruebas y demostración
-- =============================================================================

USE resenas_cursos;

-- Limpieza previa en orden de integridad referencial
DELETE FROM reviews;
DELETE FROM courses;
DELETE FROM users;

-- -----------------------------------------------------------------------------
-- 1. USUARIOS FICTICIOS Y MODERADOR DE DESARROLLO
-- Contraseña en texto plano para todos: Password123!
-- Hash BCrypt: $2a$10$wefoOYBUPTaiB8u8VA7wVe/7Hj43ckinsToxw1A8eqlwWJBdX2d7O
-- -----------------------------------------------------------------------------

INSERT INTO users (id, name, email, password_hash, role, active) VALUES
(1, 'Moderador Principal', 'moderador@correo.com', '$2a$10$wefoOYBUPTaiB8u8VA7wVe/7Hj43ckinsToxw1A8eqlwWJBdX2d7O', 'MODERATOR', TRUE),
(2, 'Juan Pérez', 'juan.perez@correo.com', '$2a$10$wefoOYBUPTaiB8u8VA7wVe/7Hj43ckinsToxw1A8eqlwWJBdX2d7O', 'USER', TRUE),
(3, 'María García', 'maria.garcia@correo.com', '$2a$10$wefoOYBUPTaiB8u8VA7wVe/7Hj43ckinsToxw1A8eqlwWJBdX2d7O', 'USER', TRUE),
(4, 'Carlos López', 'carlos.lopez@correo.com', '$2a$10$wefoOYBUPTaiB8u8VA7wVe/7Hj43ckinsToxw1A8eqlwWJBdX2d7O', 'USER', TRUE),
(5, 'Ana Torres', 'ana.torres@correo.com', '$2a$10$wefoOYBUPTaiB8u8VA7wVe/7Hj43ckinsToxw1A8eqlwWJBdX2d7O', 'USER', TRUE);

-- -----------------------------------------------------------------------------
-- 2. CURSOS (8 cursos requeridos con diversas categorías)
-- -----------------------------------------------------------------------------

INSERT INTO courses (id, title, description, category, active) VALUES
(1, 'Spring Boot 3 y Microservicios Prácticos', 'Aprende a construir APIs REST empresariales, persistencia con JPA, seguridad con Spring Security y despliegue modular.', 'Backend', TRUE),
(2, 'React y TypeScript desde Cero a Producción', 'Domina componentes funcionales, hooks avanzados, tipado estricto y consumo eficiente de APIs REST.', 'Frontend', TRUE),
(3, 'Arquitectura de Software y Patrones de Diseño', 'Fundamentos de diseño orientado a objetos, principios SOLID y patrones de diseño aplicados al desarrollo corporativo.', 'Arquitectura', TRUE),
(4, 'Bases de Datos Relacionales y SQL Avanzado', 'Modelado relacional, normalización, optimización de consultas, índices y procedimientos almacenados en MySQL.', 'Bases de Datos', TRUE),
(5, 'Docker y Contenedores para Desarrolladores', 'Creación de imágenes ligeras, docker-compose para entornos multicapa y buenas prácticas de contenedorización.', 'DevOps', TRUE),
(6, 'Seguridad Web y Autenticación con JWT', 'Aseguramiento de endpoints, políticas CORS, hashing seguro de contraseñas y mitigación de vulnerabilidades OWASP.', 'Seguridad', TRUE),
(7, 'Fundamentos de Machine Learning con Python', 'Introducción a modelos supervisados, no supervisados, preprocesamiento de datos y evaluación de métricas.', 'Inteligencia Artificial', TRUE),
(8, 'Desarrollo de Aplicaciones Móviles con Flutter', 'Creación de interfaces fluidas y reactivas para Android e iOS compartiendo una única base de código en Dart.', 'Móvil', TRUE);

-- -----------------------------------------------------------------------------
-- 3. RESEÑAS CON ESTADOS PENDING, APPROVED Y REJECTED
-- Unicidad respetada: máximo una reseña por usuario por curso
-- -----------------------------------------------------------------------------

INSERT INTO reviews (id, user_id, course_id, rating, comment, status, created_at, updated_at) VALUES
-- Reseñas Aprobadas (APPROVED - Visibles públicamente)
(1, 2, 1, 5, 'Excelente curso de Spring Boot. Las explicaciones sobre la inyección de dependencias y Spring Security fueron muy claras.', 'APPROVED', NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY),
(2, 3, 1, 4, 'Muy buen contenido práctico, los ejemplos con Maven y JPA ayudan a entender la estructura en capas.', 'APPROVED', NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 4 DAY),
(3, 4, 2, 5, 'El mejor curso para iniciar con TypeScript en React. La integración con Bootstrap quedó impecable.', 'APPROVED', NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY),
(4, 2, 3, 4, 'Los principios SOLID explicados con casos reales. Recomendado para cualquiera que busque su primera práctica.', 'APPROVED', NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY),

-- Reseñas Pendientes de Moderación (PENDING - Solo visibles por su autor y por el moderador)
(5, 3, 4, 5, 'El temario de índices y planes de ejecución en MySQL es sumamente detallado y útil.', 'PENDING', NOW() - INTERVAL 12 HOUR, NOW() - INTERVAL 12 HOUR),
(6, 4, 5, 4, 'Buenas explicaciones sobre cómo aislar contenedores para bases de datos y backend.', 'PENDING', NOW() - INTERVAL 6 HOUR, NOW() - INTERVAL 6 HOUR),
(7, 5, 1, 5, 'Me ayudó a estructurar la arquitectura de mi proyecto universitario. Muy recomendado.', 'PENDING', NOW() - INTERVAL 2 HOUR, NOW() - INTERVAL 2 HOUR),

-- Reseñas Rechazadas (REJECTED - Editables por el autor para corregirlas y enviarlas a PENDING)
(8, 5, 6, 1, 'No me gustó el curso, faltó más información.', 'REJECTED', NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY);
