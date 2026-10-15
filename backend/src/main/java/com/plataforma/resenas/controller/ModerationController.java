package com.plataforma.resenas.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.plataforma.resenas.dto.CambiarEstadoResenaDto;
import com.plataforma.resenas.dto.ResenaRespuestaDto;
import com.plataforma.resenas.entity.ReviewStatus;
import com.plataforma.resenas.service.ReviewService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/moderation")
@Tag(name = "Moderación", description = "Endpoints de moderación para usuarios con rol MODERATOR")
@SecurityRequirement(name = "BearerAuth")
public class ModerationController {

    private final ReviewService servicioResenas;

    public ModerationController(ReviewService servicioResenas) {
        this.servicioResenas = servicioResenas;
    }

    @GetMapping("/reviews")
    @Operation(summary = "Listar reseñas para moderación", description = "Permite filtrar por estado (PENDING, APPROVED, REJECTED) o ver todas. Solo accesible por MODERATOR.")
    public ResponseEntity<List<ResenaRespuestaDto>> listarResenasModeracion(
            @RequestParam(name = "status", required = false) ReviewStatus estadoFiltro) {

        List<ResenaRespuestaDto> resenas = servicioResenas.listarResenasParaModeracion(estadoFiltro);
        return ResponseEntity.ok(resenas);
    }

    @PatchMapping("/reviews/{id}/status")
    @Operation(summary = "Aprobar o rechazar reseña", description = "Cambia el estado de una reseña en PENDING a APPROVED o REJECTED. Solo accesible por MODERATOR.")
    public ResponseEntity<ResenaRespuestaDto> cambiarEstadoResena(
            @PathVariable("id") Long idResena,
            @Valid @RequestBody CambiarEstadoResenaDto peticion,
            Authentication autenticacion) {

        String correoModerador = autenticacion.getName();
        ResenaRespuestaDto resenaModerada = servicioResenas.cambiarEstadoModeracion(idResena, peticion, correoModerador);
        return ResponseEntity.ok(resenaModerada);
    }
}
