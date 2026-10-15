package com.plataforma.resenas.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.plataforma.resenas.dto.LoginPeticionDto;
import com.plataforma.resenas.dto.LoginRespuestaDto;
import com.plataforma.resenas.dto.RegistroPeticionDto;
import com.plataforma.resenas.dto.UsuarioRespuestaDto;
import com.plataforma.resenas.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Endpoints de registro público e inicio de sesión con JWT")
public class AuthController {

    private final AuthService servicioAutenticacion;

    public AuthController(AuthService servicioAutenticacion) {
        this.servicioAutenticacion = servicioAutenticacion;
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar nuevo usuario", description = "El registro público siempre asigna el rol USER.")
    public ResponseEntity<UsuarioRespuestaDto> registrar(@Valid @RequestBody RegistroPeticionDto peticion) {
        UsuarioRespuestaDto nuevoUsuario = servicioAutenticacion.registrarUsuario(peticion);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoUsuario);
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Valida credenciales y genera el token Bearer JWT.")
    public ResponseEntity<LoginRespuestaDto> login(@Valid @RequestBody LoginPeticionDto peticion) {
        LoginRespuestaDto respuesta = servicioAutenticacion.autenticarUsuario(peticion);
        return ResponseEntity.ok(respuesta);
    }
}
