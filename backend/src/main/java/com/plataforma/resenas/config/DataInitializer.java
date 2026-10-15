package com.plataforma.resenas.config;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.plataforma.resenas.entity.AppUser;
import com.plataforma.resenas.entity.Course;
import com.plataforma.resenas.entity.Review;
import com.plataforma.resenas.entity.ReviewStatus;
import com.plataforma.resenas.entity.Role;
import com.plataforma.resenas.repository.AppUserRepository;
import com.plataforma.resenas.repository.CourseRepository;
import com.plataforma.resenas.repository.ReviewRepository;

@Component
@Profile("dev")
public class DataInitializer implements CommandLineRunner {

    private final AppUserRepository repositorioUsuarios;
    private final CourseRepository repositorioCursos;
    private final ReviewRepository repositorioResenas;
    private final PasswordEncoder codificadorContrasena;

    public DataInitializer(
            AppUserRepository repositorioUsuarios,
            CourseRepository repositorioCursos,
            ReviewRepository repositorioResenas,
            PasswordEncoder codificadorContrasena) {

        this.repositorioUsuarios = repositorioUsuarios;
        this.repositorioCursos = repositorioCursos;
        this.repositorioResenas = repositorioResenas;
        this.codificadorContrasena = codificadorContrasena;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (repositorioCursos.count() > 0) {
            return;
        }

        inicializarDatosDesarrollo();
    }

    private void inicializarDatosDesarrollo() {
        String contrasenaHasheada = codificadorContrasena.encode("Password123!");

        // 1. Usuarios de desarrollo
        AppUser moderador = new AppUser("Moderador Principal", "moderador@correo.com", contrasenaHasheada, Role.MODERATOR);
        AppUser usuarioJuan = new AppUser("Juan Pérez", "juan.perez@correo.com", contrasenaHasheada, Role.USER);
        AppUser usuarioMaria = new AppUser("María García", "maria.garcia@correo.com", contrasenaHasheada, Role.USER);
        AppUser usuarioCarlos = new AppUser("Carlos López", "carlos.lopez@correo.com", contrasenaHasheada, Role.USER);
        AppUser usuarioAna = new AppUser("Ana Torres", "ana.torres@correo.com", contrasenaHasheada, Role.USER);

        repositorioUsuarios.saveAll(List.of(moderador, usuarioJuan, usuarioMaria, usuarioCarlos, usuarioAna));

        // 2. Cursos iniciales (8 cursos exigidos)
        Course curso1 = new Course("Spring Boot 3 y Microservicios Prácticos", "Aprende a construir APIs REST empresariales, persistencia con JPA, seguridad con Spring Security y despliegue modular.", "Backend", true);
        Course curso2 = new Course("React y TypeScript desde Cero a Producción", "Domina componentes funcionales, hooks avanzados, tipado estricto y consumo eficiente de APIs REST.", "Frontend", true);
        Course curso3 = new Course("Arquitectura de Software y Patrones de Diseño", "Fundamentos de diseño orientado a objetos, principios SOLID y patrones de diseño aplicados al desarrollo corporativo.", "Arquitectura", true);
        Course curso4 = new Course("Bases de Datos Relacionales y SQL Avanzado", "Modelado relacional, normalización, optimización de consultas, índices y procedimientos almacenados en MySQL.", "Bases de Datos", true);
        Course curso5 = new Course("Docker y Contenedores para Desarrolladores", "Creación de imágenes ligeras, docker-compose para entornos multicapa y buenas prácticas de contenedorización.", "DevOps", true);
        Course curso6 = new Course("Seguridad Web y Autenticación con JWT", "Aseguramiento de endpoints, políticas CORS, hashing seguro de contraseñas y mitigación de vulnerabilidades OWASP.", "Seguridad", true);
        Course curso7 = new Course("Fundamentos de Machine Learning con Python", "Introducción a modelos supervisados, no supervisados, preprocesamiento de datos y evaluación de métricas.", "Inteligencia Artificial", true);
        Course curso8 = new Course("Desarrollo de Aplicaciones Móviles con Flutter", "Creación de interfaces fluidas y reactivas para Android e iOS compartiendo una única base de código en Dart.", "Móvil", true);

        repositorioCursos.saveAll(List.of(curso1, curso2, curso3, curso4, curso5, curso6, curso7, curso8));

        // 3. Reseñas iniciales en diferentes estados
        Instant ahora = Instant.now();

        Review resena1 = new Review(usuarioJuan, curso1, 5, "Excelente curso de Spring Boot. Las explicaciones sobre la inyección de dependencias y Spring Security fueron muy claras.");
        resena1.setStatus(ReviewStatus.APPROVED);
        resena1.setCreatedAt(ahora.minus(5, ChronoUnit.DAYS));
        resena1.setUpdatedAt(ahora.minus(5, ChronoUnit.DAYS));

        Review resena2 = new Review(usuarioMaria, curso1, 4, "Muy buen contenido práctico, los ejemplos con Maven y JPA ayudan a entender la estructura en capas.");
        resena2.setStatus(ReviewStatus.APPROVED);
        resena2.setCreatedAt(ahora.minus(4, ChronoUnit.DAYS));
        resena2.setUpdatedAt(ahora.minus(4, ChronoUnit.DAYS));

        Review resena3 = new Review(usuarioCarlos, curso2, 5, "El mejor curso para iniciar con TypeScript en React. La integración con Bootstrap quedó impecable.");
        resena3.setStatus(ReviewStatus.APPROVED);
        resena3.setCreatedAt(ahora.minus(3, ChronoUnit.DAYS));
        resena3.setUpdatedAt(ahora.minus(3, ChronoUnit.DAYS));

        Review resena4 = new Review(usuarioJuan, curso3, 4, "Los principios SOLID explicados con casos reales. Recomendado para cualquiera que busque su primera práctica.");
        resena4.setStatus(ReviewStatus.APPROVED);
        resena4.setCreatedAt(ahora.minus(2, ChronoUnit.DAYS));
        resena4.setUpdatedAt(ahora.minus(2, ChronoUnit.DAYS));

        Review resena5 = new Review(usuarioMaria, curso4, 5, "El temario de índices y planes de ejecución en MySQL es sumamente detallado y útil.");
        resena5.setStatus(ReviewStatus.PENDING);
        resena5.setCreatedAt(ahora.minus(12, ChronoUnit.HOURS));
        resena5.setUpdatedAt(ahora.minus(12, ChronoUnit.HOURS));

        Review resena6 = new Review(usuarioCarlos, curso5, 4, "Buenas explicaciones sobre cómo aislar contenedores para bases de datos y backend.");
        resena6.setStatus(ReviewStatus.PENDING);
        resena6.setCreatedAt(ahora.minus(6, ChronoUnit.HOURS));
        resena6.setUpdatedAt(ahora.minus(6, ChronoUnit.HOURS));

        Review resena7 = new Review(usuarioAna, curso6, 1, "No me gustó el curso, faltó más información.");
        resena7.setStatus(ReviewStatus.REJECTED);
        resena7.setCreatedAt(ahora.minus(1, ChronoUnit.DAYS));
        resena7.setUpdatedAt(ahora.minus(1, ChronoUnit.DAYS));

        repositorioResenas.saveAll(List.of(resena1, resena2, resena3, resena4, resena5, resena6, resena7));
    }
}
