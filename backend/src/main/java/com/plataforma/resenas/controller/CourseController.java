package com.plataforma.resenas.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.plataforma.resenas.dto.CursoRespuestaDto;
import com.plataforma.resenas.service.CourseService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/courses")
@Tag(name = "Cursos", description = "Endpoints públicos para consultar el catálogo de cursos")
public class CourseController {

    private final CourseService servicioCursos;

    public CourseController(CourseService servicioCursos) {
        this.servicioCursos = servicioCursos;
    }

    @GetMapping
    @Operation(summary = "Listar cursos activos", description = "Retorna todos los cursos disponibles junto con su calificación promedio y total de reseñas.")
    public ResponseEntity<List<CursoRespuestaDto>> listarCursos() {
        List<CursoRespuestaDto> cursos = servicioCursos.listarCursosActivos();
        return ResponseEntity.ok(cursos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de un curso", description = "Consulta la información de un curso por su ID.")
    public ResponseEntity<CursoRespuestaDto> obtenerCursoPorId(@PathVariable("id") Long idCurso) {
        CursoRespuestaDto curso = servicioCursos.obtenerCursoPorId(idCurso);
        return ResponseEntity.ok(curso);
    }
}
