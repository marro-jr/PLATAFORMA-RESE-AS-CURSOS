package com.plataforma.resenas;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plataforma.resenas.dto.LoginPeticionDto;
import com.plataforma.resenas.dto.RegistroPeticionDto;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapeadorObjetos;

    @Test
    @DisplayName("Registro exitoso asigna automáticamente el rol USER")
    void registrarUsuarioValido_debeRetornar201YRolUser() throws Exception {
        RegistroPeticionDto peticion = new RegistroPeticionDto(
                "Estudiante Prueba",
                "estudiante.prueba@correo.com",
                "Password123!",
                null
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapeadorObjetos.writeValueAsString(peticion)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Estudiante Prueba"))
                .andExpect(jsonPath("$.correo").value("estudiante.prueba@correo.com"))
                .andExpect(jsonPath("$.rol").value("USER"))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    @DisplayName("Registro que intenta asignarse MODERATOR es ignorado y fuerza rol USER")
    void registrarUsuarioConIntentoModerador_debeForzarRolUser() throws Exception {
        RegistroPeticionDto peticion = new RegistroPeticionDto(
                "Infiltrado",
                "hacker@correo.com",
                "Password123!",
                "MODERATOR"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapeadorObjetos.writeValueAsString(peticion)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.correo").value("hacker@correo.com"))
                .andExpect(jsonPath("$.rol").value("USER"));
    }

    @Test
    @DisplayName("Login válido genera token Bearer JWT y retorna datos de sesión")
    void loginValido_debeRetornarTokenJWT() throws Exception {
        LoginPeticionDto peticion = new LoginPeticionDto("juan.perez@correo.com", "Password123!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapeadorObjetos.writeValueAsString(peticion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.tipoToken").value("Bearer"))
                .andExpect(jsonPath("$.rol").value("USER"))
                .andExpect(jsonPath("$.correo").value("juan.perez@correo.com"));
    }

    @Test
    @DisplayName("Login incorrecto con contraseña errónea debe retornar 401 Unauthorized")
    void loginIncorrecto_debeRetornar401() throws Exception {
        LoginPeticionDto peticion = new LoginPeticionDto("juan.perez@correo.com", "ClaveEquivocada999");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapeadorObjetos.writeValueAsString(peticion)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Petición a recurso protegido sin JWT debe retornar 401 Unauthorized")
    void jwtAusente_debeRetornar401() throws Exception {
        mockMvc.perform(get("/api/me/reviews"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Petición con JWT alterado o corrupto debe retornar 401 Unauthorized")
    void jwtAlterado_debeRetornar401() throws Exception {
        mockMvc.perform(get("/api/me/reviews")
                        .header("Authorization", "Bearer token_falso_invalido_12345"))
                .andExpect(status().isUnauthorized());
    }
}
