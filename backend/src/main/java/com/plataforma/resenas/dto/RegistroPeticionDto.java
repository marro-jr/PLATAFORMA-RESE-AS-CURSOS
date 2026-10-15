package com.plataforma.resenas.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroPeticionDto(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 120, message = "El nombre debe tener entre 2 y 120 caracteres")
        String nombre,

        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "El formato de correo no es válido")
        String correo,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
        String clave,

        // Campo opcional para comprobar que el backend jamás permita autoasignarse rol MODERATOR
        String rol
) {}
