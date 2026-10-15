package com.plataforma.resenas.dto;

public record LoginRespuestaDto(
        String token,
        String tipoToken,
        String rol,
        String correo,
        String nombre
) {}
