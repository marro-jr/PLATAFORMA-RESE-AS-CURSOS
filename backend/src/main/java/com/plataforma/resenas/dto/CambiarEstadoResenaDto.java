package com.plataforma.resenas.dto;

import com.plataforma.resenas.entity.ReviewStatus;

import jakarta.validation.constraints.NotNull;

public record CambiarEstadoResenaDto(
        @NotNull(message = "El nuevo estado es obligatorio")
        ReviewStatus estado
) {}
