package com.plataforma.resenas.dto;

import com.plataforma.resenas.entity.Role;

public record UsuarioRespuestaDto(
        Long id,
        String nombre,
        String correo,
        Role rol,
        boolean activo
) {}
