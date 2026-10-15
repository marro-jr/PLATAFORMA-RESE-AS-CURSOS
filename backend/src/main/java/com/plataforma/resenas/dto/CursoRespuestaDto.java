package com.plataforma.resenas.dto;

public record CursoRespuestaDto(
        Long id,
        String titulo,
        String descripcion,
        String categoria,
        boolean activo,
        Double promedioCalificacion,
        Long totalResenas
) {}
