package com.plataforma.resenas;

import java.util.Base64;
import java.util.Collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import com.plataforma.resenas.security.JwtService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    // Clave válida de al menos 256 bits (32 bytes) codificada en Base64
    private static final String CLAVE_TEST_VALIDA =
            "Y2xhdmUtc2VjcmV0YS1leGNsdXNpdmEtcGFyYS1wcnVlYmFzLXVuaXRhcmlhcy1lLWludGVncmFjaW9uLTMyYnl0ZXM=";

    @Test
    @DisplayName("Inicialización correcta con clave válida en Base64 de más de 256 bits")
    void inicializarConClaveValida_debeFuncionarCorrectamente() {
        JwtService servicioJwt = new JwtService(CLAVE_TEST_VALIDA, 3600);
        String token = servicioJwt.generarToken("estudiante@correo.com", "USER", "Estudiante");

        assertNotNull(token);
        assertEquals("estudiante@correo.com", servicioJwt.obtenerCorreo(token));
        assertEquals("USER", servicioJwt.obtenerRol(token));
        assertEquals("Estudiante", servicioJwt.obtenerNombre(token));
    }

    @Test
    @DisplayName("Lanza excepción si la clave secreta es nula o vacía")
    void inicializarConClaveNulaOVacia_debeLanzarExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> new JwtService(null, 3600));
        assertThrows(IllegalArgumentException.class, () -> new JwtService("", 3600));
        assertThrows(IllegalArgumentException.class, () -> new JwtService("   ", 3600));
    }

    @Test
    @DisplayName("Lanza excepción si la clave no tiene formato Base64 válido")
    void inicializarConClaveBase64Invalida_debeLanzarExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> new JwtService("clave_invalida_no_base64!@#$", 3600));
    }

    @Test
    @DisplayName("Lanza excepción si la clave tiene menos de 256 bits (32 bytes)")
    void inicializarConClaveCorta_debeLanzarExcepcion() {
        // Clave de 10 bytes en Base64 (menos de 32 bytes)
        String claveCortaBase64 = Base64.getEncoder().encodeToString("claveCorta".getBytes());
        assertThrows(IllegalArgumentException.class, () -> new JwtService(claveCortaBase64, 3600));
    }

    @Test
    @DisplayName("Validación de token con UserDetails coincidente")
    void esTokenValido_conUsuarioCoincidente_retornaTrue() {
        JwtService servicioJwt = new JwtService(CLAVE_TEST_VALIDA, 3600);
        String token = servicioJwt.generarToken("usuario@correo.com", "USER", "Usuario");

        UserDetails usuario = new User("usuario@correo.com", "pass", Collections.emptyList());
        assertTrue(servicioJwt.esTokenValido(token, usuario));

        UserDetails otroUsuario = new User("otro@correo.com", "pass", Collections.emptyList());
        assertFalse(servicioJwt.esTokenValido(token, otroUsuario));
    }
}
