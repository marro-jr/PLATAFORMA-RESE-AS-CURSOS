package com.plataforma.resenas.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginPeticionDto(
        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "El formato de correo no es válido")
        String correo,

        @NotBlank(message = "La contraseña es obligatoria")
        String clave
) {}
