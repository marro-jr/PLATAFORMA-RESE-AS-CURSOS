package com.plataforma.resenas.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.plataforma.resenas.dto.ActualizarResenaDto;
import com.plataforma.resenas.dto.CrearResenaDto;
import com.plataforma.resenas.dto.ResenaRespuestaDto;
import com.plataforma.resenas.service.ReviewService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Tag(name = "Reseñas", description = "Endpoints para la gestión, publicación y consulta de reseñas")
public class ReviewController {

    private final ReviewService servicioResenas;

    public ReviewController(ReviewService servicioResenas) {
        this.servicioResenas = servicioResenas;
    }

    @GetMapping("/api/courses/{id}/reviews")
    @Operation(summary = "Listar reseñas públicas de un curso", description = "Retorna únicamente las reseñas con estado APPROVED.")
    public ResponseEntity<List<ResenaRespuestaDto>> listarResenasAprobadasPorCurso(@PathVariable("id") Long idCurso) {
        List<ResenaRespuestaDto> resenas = servicioResenas.listarResenasAprobadasPorCurso(idCurso);
        return ResponseEntity.ok(resenas);
    }

    @PostMapping("/api/courses/{id}/reviews")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Publicar reseña para un curso", description = "Crea una nueva reseña en estado PENDING. Requiere rol USER.")
    public ResponseEntity<ResenaRespuestaDto> crearResena(
            @PathVariable("id") Long idCurso,
            @Valid @RequestBody CrearResenaDto peticion,
            Authentication autenticacion) {

        String correoUsuario = autenticacion.getName();
        ResenaRespuestaDto resenaCreada = servicioResenas.crearResena(idCurso, peticion, correoUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(resenaCreada);
    }

    @GetMapping("/api/me/reviews")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Consultar mis reseñas", description = "Retorna el listado completo de reseñas del usuario autenticado con su estado de moderación.")
    public ResponseEntity<List<ResenaRespuestaDto>> listarMisResenas(Authentication autenticacion) {
        String correoUsuario = autenticacion.getName();
        List<ResenaRespuestaDto> misResenas = servicioResenas.listarMisResenas(correoUsuario);
        return ResponseEntity.ok(misResenas);
    }

    @PatchMapping("/api/reviews/{id}")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Editar reseña propia", description = "Permite editar una reseña propia en estado PENDING o REJECTED. Si estaba REJECTED regresa a PENDING.")
    public ResponseEntity<ResenaRespuestaDto> actualizarResenaPropia(
            @PathVariable("id") Long idResena,
            @Valid @RequestBody ActualizarResenaDto peticion,
            Authentication autenticacion) {

        String correoUsuario = autenticacion.getName();
        ResenaRespuestaDto resenaActualizada = servicioResenas.actualizarResenaPropia(idResena, peticion, correoUsuario);
        return ResponseEntity.ok(resenaActualizada);
    }
}
