package com.plataforma.resenas.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.plataforma.resenas.dto.CursoRespuestaDto;
import com.plataforma.resenas.entity.Course;
import com.plataforma.resenas.entity.Review;
import com.plataforma.resenas.entity.ReviewStatus;
import com.plataforma.resenas.exception.RecursoNoEncontradoException;
import com.plataforma.resenas.repository.CourseRepository;
import com.plataforma.resenas.repository.ReviewRepository;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final ReviewRepository reviewRepository;

    public CourseService(CourseRepository courseRepository, ReviewRepository reviewRepository) {
        this.courseRepository = courseRepository;
        this.reviewRepository = reviewRepository;
    }

    @Transactional(readOnly = true)
    public List<CursoRespuestaDto> listarCursosActivos() {
        List<Course> cursos = courseRepository.findByActiveTrue();
        return cursos.stream()
                .map(this::convertirACursoRespuestaDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public CursoRespuestaDto obtenerCursoPorId(Long idCurso) {
        Course curso = courseRepository.findByIdAndActiveTrue(idCurso)
                .orElseThrow(() -> new RecursoNoEncontradoException("Curso no encontrado con id: " + idCurso));

        return convertirACursoRespuestaDto(curso);
    }

    private CursoRespuestaDto convertirACursoRespuestaDto(Course curso) {
        List<Review> resenasAprobadas = reviewRepository.findByCourseIdAndStatusOrderByCreatedAtDesc(
                curso.getId(),
                ReviewStatus.APPROVED
        );

        long total = resenasAprobadas.size();
        Double promedio = total > 0
                ? resenasAprobadas.stream().mapToInt(Review::getRating).average().orElse(0.0)
                : 0.0;

        // Redondear a un decimal
        double promedioRedondeado = Math.round(promedio * 10.0) / 10.0;

        return new CursoRespuestaDto(
                curso.getId(),
                curso.getTitle(),
                curso.getDescription(),
                curso.getCategory(),
                curso.isActive(),
                promedioRedondeado,
                total
        );
    }
}
