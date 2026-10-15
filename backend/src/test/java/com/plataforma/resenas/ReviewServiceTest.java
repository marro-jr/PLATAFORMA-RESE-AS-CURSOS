package com.plataforma.resenas;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.plataforma.resenas.dto.ActualizarResenaDto;
import com.plataforma.resenas.dto.CambiarEstadoResenaDto;
import com.plataforma.resenas.dto.CrearResenaDto;
import com.plataforma.resenas.entity.AppUser;
import com.plataforma.resenas.entity.Course;
import com.plataforma.resenas.entity.Review;
import com.plataforma.resenas.entity.ReviewStatus;
import com.plataforma.resenas.entity.Role;
import com.plataforma.resenas.exception.AccesoDenegadoException;
import com.plataforma.resenas.exception.ReglaNegocioException;
import com.plataforma.resenas.repository.AppUserRepository;
import com.plataforma.resenas.repository.CourseRepository;
import com.plataforma.resenas.repository.ReviewRepository;
import com.plataforma.resenas.service.ReviewService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository repositorioResenas;

    @Mock
    private CourseRepository repositorioCursos;

    @Mock
    private AppUserRepository repositorioUsuarios;

    @InjectMocks
    private ReviewService servicioResenas;

    @Test
    @DisplayName("Lanza excepción si se intenta publicar una reseña en un curso inactivo")
    void crearResena_cursoInactivo_lanzaExcepcion() {
        Course cursoInactivo = new Course(1L, "Curso Inactivo", "Desc", "Cat", false);
        when(repositorioCursos.findById(1L)).thenReturn(Optional.of(cursoInactivo));

        CrearResenaDto peticion = new CrearResenaDto(5, "Buen curso");

        ReglaNegocioException excepcion = assertThrows(
                ReglaNegocioException.class,
                () -> servicioResenas.crearResena(1L, peticion, "usuario@correo.com")
        );

        assertEquals("No se pueden publicar reseñas para un curso inactivo", excepcion.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, excepcion.getCodigoEstado());
        verify(repositorioResenas, never()).save(any());
    }

    @Test
    @DisplayName("Lanza conflicto si el usuario ya tiene una reseña para ese curso")
    void crearResena_duplicada_lanzaConflicto() {
        Course curso = new Course(1L, "Curso Java", "Desc", "Backend", true);
        AppUser usuario = new AppUser(2L, "Juan", "juan@correo.com", "hash", Role.USER, true);

        when(repositorioCursos.findById(1L)).thenReturn(Optional.of(curso));
        when(repositorioUsuarios.findByEmail("juan@correo.com")).thenReturn(Optional.of(usuario));
        when(repositorioResenas.existsByUserIdAndCourseId(2L, 1L)).thenReturn(true);

        CrearResenaDto peticion = new CrearResenaDto(4, "Segunda reseña");

        ReglaNegocioException excepcion = assertThrows(
                ReglaNegocioException.class,
                () -> servicioResenas.crearResena(1L, peticion, "juan@correo.com")
        );

        assertEquals("Ya has publicado una reseña para este curso", excepcion.getMessage());
        assertEquals(HttpStatus.CONFLICT, excepcion.getCodigoEstado());
        verify(repositorioResenas, never()).save(any());
    }

    @Test
    @DisplayName("Lanza acceso denegado si un usuario intenta editar una reseña ajena")
    void actualizarResenaPropia_usuarioDistinto_lanzaAccesoDenegado() {
        AppUser autor = new AppUser(1L, "Autor", "autor@correo.com", "hash", Role.USER, true);
        Course curso = new Course(1L, "Curso", "Desc", "Cat", true);
        Review resena = new Review(autor, curso, 4, "Comentario");
        resena.setId(10L);

        when(repositorioResenas.findById(10L)).thenReturn(Optional.of(resena));

        ActualizarResenaDto peticion = new ActualizarResenaDto(5, "Comentario modificado");

        assertThrows(
                AccesoDenegadoException.class,
                () -> servicioResenas.actualizarResenaPropia(10L, peticion, "otro.usuario@correo.com")
        );
        verify(repositorioResenas, never()).save(any());
    }

    @Test
    @DisplayName("Lanza excepción si se intenta editar una reseña con estado APPROVED")
    void actualizarResenaPropia_resenaAprobada_lanzaReglaNegocioException() {
        AppUser autor = new AppUser(1L, "Autor", "autor@correo.com", "hash", Role.USER, true);
        Course curso = new Course(1L, "Curso", "Desc", "Cat", true);
        Review resena = new Review(autor, curso, 5, "Comentario aprobado");
        resena.setId(10L);
        resena.setStatus(ReviewStatus.APPROVED);

        when(repositorioResenas.findById(10L)).thenReturn(Optional.of(resena));

        ActualizarResenaDto peticion = new ActualizarResenaDto(4, "Intento de edición");

        ReglaNegocioException excepcion = assertThrows(
                ReglaNegocioException.class,
                () -> servicioResenas.actualizarResenaPropia(10L, peticion, "autor@correo.com")
        );

        assertEquals("Una reseña aprobada no puede ser modificada en esta versión", excepcion.getMessage());
        verify(repositorioResenas, never()).save(any());
    }

    @Test
    @DisplayName("Lanza excepción si el moderador intenta cambiar el estado de una reseña que ya fue moderada")
    void cambiarEstadoModeracion_yaModerada_lanzaReglaNegocioException() {
        AppUser autor = new AppUser(1L, "Autor", "autor@correo.com", "hash", Role.USER, true);
        Course curso = new Course(1L, "Curso", "Desc", "Cat", true);
        Review resena = new Review(autor, curso, 5, "Comentario");
        resena.setId(20L);
        resena.setStatus(ReviewStatus.APPROVED);

        when(repositorioResenas.findById(20L)).thenReturn(Optional.of(resena));

        CambiarEstadoResenaDto peticion = new CambiarEstadoResenaDto(ReviewStatus.REJECTED);

        ReglaNegocioException excepcion = assertThrows(
                ReglaNegocioException.class,
                () -> servicioResenas.cambiarEstadoModeracion(20L, peticion, "moderador@correo.com")
        );

        assertEquals("No se puede cambiar el estado de una reseña que ya fue moderada previamente", excepcion.getMessage());
        verify(repositorioResenas, never()).save(any());
    }
}
