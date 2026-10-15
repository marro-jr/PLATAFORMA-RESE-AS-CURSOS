package com.plataforma.resenas.dto;

import java.time.Instant;

import com.plataforma.resenas.entity.ReviewStatus;

public record ResenaRespuestaDto(
        Long id,
        Long idCurso,
        String tituloCurso,
        Long idUsuario,
        String nombreUsuario,
        String correoUsuario,
        Integer calificacion,
        String comentario,
        ReviewStatus estado,
        Instant fechaCreacion,
        Instant fechaActualizacion
) {}
