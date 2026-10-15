package com.plataforma.resenas.service;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.plataforma.resenas.dto.ActualizarResenaDto;
import com.plataforma.resenas.dto.CambiarEstadoResenaDto;
import com.plataforma.resenas.dto.CrearResenaDto;
import com.plataforma.resenas.dto.ResenaRespuestaDto;
import com.plataforma.resenas.entity.AppUser;
import com.plataforma.resenas.entity.Course;
import com.plataforma.resenas.entity.Review;
import com.plataforma.resenas.entity.ReviewStatus;
import com.plataforma.resenas.exception.AccesoDenegadoException;
import com.plataforma.resenas.exception.RecursoNoEncontradoException;
import com.plataforma.resenas.exception.ReglaNegocioException;
import com.plataforma.resenas.repository.AppUserRepository;
import com.plataforma.resenas.repository.CourseRepository;
import com.plataforma.resenas.repository.ReviewRepository;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final CourseRepository courseRepository;
    private final AppUserRepository appUserRepository;

    public ReviewService(
            ReviewRepository reviewRepository,
            CourseRepository courseRepository,
            AppUserRepository appUserRepository) {

        this.reviewRepository = reviewRepository;
        this.courseRepository = courseRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional(readOnly = true)
    public List<ResenaRespuestaDto> listarResenasAprobadasPorCurso(Long idCurso) {
        Course curso = courseRepository.findByIdAndActiveTrue(idCurso)
                .orElseThrow(() -> new RecursoNoEncontradoException("Curso no encontrado o inactivo con id: " + idCurso));

        List<Review> resenas = reviewRepository.findByCourseIdAndStatusOrderByCreatedAtDesc(
                curso.getId(),
                ReviewStatus.APPROVED
        );

        return resenas.stream()
                .map(this::convertirAResenaRespuestaDto)
                .toList();
    }

    @Transactional
    public ResenaRespuestaDto crearResena(Long idCurso, CrearResenaDto peticion, String correoUsuario) {
        Course curso = courseRepository.findById(idCurso)
                .orElseThrow(() -> new RecursoNoEncontradoException("Curso no encontrado con id: " + idCurso));

        if (!curso.isActive()) {
            throw new ReglaNegocioException("No se pueden publicar reseñas para un curso inactivo", HttpStatus.BAD_REQUEST);
        }

        AppUser usuario = appUserRepository.findByEmail(correoUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con correo: " + correoUsuario));

        if (!usuario.isActive()) {
            throw new ReglaNegocioException("La cuenta del usuario está inactiva", HttpStatus.FORBIDDEN);
        }

        if (reviewRepository.existsByUserIdAndCourseId(usuario.getId(), curso.getId())) {
            throw new ReglaNegocioException("Ya has publicado una reseña para este curso", HttpStatus.CONFLICT);
        }

        Review nuevaResena = new Review();
        nuevaResena.setUser(usuario);
        nuevaResena.setCourse(curso);
        nuevaResena.setRating(peticion.calificacion());
        nuevaResena.setComment(peticion.comentario().trim());
        nuevaResena.setStatus(ReviewStatus.PENDING);

        Review resenaGuardada = reviewRepository.save(nuevaResena);
        return convertirAResenaRespuestaDto(resenaGuardada);
    }

    @Transactional(readOnly = true)
    public List<ResenaRespuestaDto> listarMisResenas(String correoUsuario) {
        List<Review> misResenas = reviewRepository.findByUserEmailOrderByCreatedAtDesc(correoUsuario);
        return misResenas.stream()
                .map(this::convertirAResenaRespuestaDto)
                .toList();
    }

    @Transactional
    public ResenaRespuestaDto actualizarResenaPropia(Long idResena, ActualizarResenaDto peticion, String correoUsuario) {
        Review resena = reviewRepository.findById(idResena)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reseña no encontrada con id: " + idResena));

        // Validación de pertenencia: solo el autor puede editar su reseña
        if (!resena.getUser().getEmail().equalsIgnoreCase(correoUsuario)) {
            throw new AccesoDenegadoException("No tienes permiso para editar una reseña que no es tuya");
        }

        // Validación de estado: una reseña aprobada no se puede editar
        if (resena.getStatus() == ReviewStatus.APPROVED) {
            throw new ReglaNegocioException("Una reseña aprobada no puede ser modificada en esta versión", HttpStatus.BAD_REQUEST);
        }

        // Si estaba rechazada, al editarse regresa a PENDING para re-evaluación
        if (resena.getStatus() == ReviewStatus.REJECTED) {
            resena.setStatus(ReviewStatus.PENDING);
        }

        resena.setRating(peticion.calificacion());
        resena.setComment(peticion.comentario().trim());
        resena.setUpdatedAt(Instant.now());

        Review resenaActualizada = reviewRepository.save(resena);
        return convertirAResenaRespuestaDto(resenaActualizada);
    }

    @Transactional(readOnly = true)
    public List<ResenaRespuestaDto> listarResenasParaModeracion(ReviewStatus estadoFiltro) {
        List<Review> resenas;
        if (estadoFiltro != null) {
            resenas = reviewRepository.findByStatusOrderByCreatedAtAsc(estadoFiltro);
        } else {
            resenas = reviewRepository.findAllByOrderByCreatedAtDesc();
        }

        return resenas.stream()
                .map(this::convertirAResenaRespuestaDto)
                .toList();
    }

    @Transactional
    public ResenaRespuestaDto cambiarEstadoModeracion(Long idResena, CambiarEstadoResenaDto peticion, String correoModerador) {
        Review resena = reviewRepository.findById(idResena)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reseña no encontrada con id: " + idResena));

        // Regla: No permitir moderar reseñas que ya fueron moderadas previamente
        if (resena.getStatus() != ReviewStatus.PENDING) {
            throw new ReglaNegocioException("No se puede cambiar el estado de una reseña que ya fue moderada previamente", HttpStatus.BAD_REQUEST);
        }

        if (peticion.estado() != ReviewStatus.APPROVED && peticion.estado() != ReviewStatus.REJECTED) {
            throw new ReglaNegocioException("Solo se permite aprobar o rechazar la reseña", HttpStatus.BAD_REQUEST);
        }

        resena.setStatus(peticion.estado());
        resena.setUpdatedAt(Instant.now());

        Review resenaModerada = reviewRepository.save(resena);
        return convertirAResenaRespuestaDto(resenaModerada);
    }

    private ResenaRespuestaDto convertirAResenaRespuestaDto(Review resena) {
        return new ResenaRespuestaDto(
                resena.getId(),
                resena.getCourse().getId(),
                resena.getCourse().getTitle(),
                resena.getUser().getId(),
                resena.getUser().getName(),
                resena.getUser().getEmail(),
                resena.getRating(),
                resena.getComment(),
                resena.getStatus(),
                resena.getCreatedAt(),
                resena.getUpdatedAt()
        );
    }
}
