package com.plataforma.resenas;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plataforma.resenas.dto.ActualizarResenaDto;
import com.plataforma.resenas.dto.CambiarEstadoResenaDto;
import com.plataforma.resenas.dto.CrearResenaDto;
import com.plataforma.resenas.dto.LoginPeticionDto;
import com.plataforma.resenas.dto.LoginRespuestaDto;
import com.plataforma.resenas.entity.ReviewStatus;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ReviewModerationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapeadorObjetos;

    private String tokenUsuarioJuan;
    private String tokenUsuarioMaria;
    private String tokenModerador;

    @BeforeEach
    void inicializarTokens() throws Exception {
        tokenUsuarioJuan = obtenerToken("juan.perez@correo.com", "Password123!");
        tokenUsuarioMaria = obtenerToken("maria.garcia@correo.com", "Password123!");
        tokenModerador = obtenerToken("moderador@correo.com", "Password123!");
    }

    private String obtenerToken(String correo, String contrasena) throws Exception {
        LoginPeticionDto peticion = new LoginPeticionDto(correo, contrasena);
        MvcResult resultado = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapeadorObjetos.writeValueAsString(peticion)))
                .andExpect(status().isOk())
                .andReturn();

        LoginRespuestaDto respuesta = mapeadorObjetos.readValue(
                resultado.getResponse().getContentAsString(),
                LoginRespuestaDto.class
        );
        return respuesta.token();
    }

    @Test
    @DisplayName("Solo reseñas APPROVED aparecen públicamente en el detalle del curso")
    void soloResenasApprovedAparecenPublicamente() throws Exception {
        // En el curso 1 hay reseñas aprobadas y también una pendiente de otro usuario
        mockMvc.perform(get("/api/courses/1/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].estado", everyItem(is("APPROVED"))));
    }

    @Test
    @DisplayName("Usuario con rol USER intenta moderar una reseña y recibe 403 Forbidden")
    void userIntentaAprobarResena_debeRetornar403() throws Exception {
        CambiarEstadoResenaDto peticion = new CambiarEstadoResenaDto(ReviewStatus.APPROVED);

        mockMvc.perform(patch("/api/moderation/reviews/5/status")
                        .header("Authorization", "Bearer " + tokenUsuarioJuan)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapeadorObjetos.writeValueAsString(peticion)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("MODERATOR aprueba una reseña PENDING exitosamente")
    void moderatorApruebaResena_debeRetornar200() throws Exception {
        // La reseña 5 está en PENDING
        CambiarEstadoResenaDto peticion = new CambiarEstadoResenaDto(ReviewStatus.APPROVED);

        mockMvc.perform(patch("/api/moderation/reviews/5/status")
                        .header("Authorization", "Bearer " + tokenModerador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapeadorObjetos.writeValueAsString(peticion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.estado").value("APPROVED"));
    }

    @Test
    @DisplayName("No permitir aprobar o rechazar reseñas que ya fueron moderadas")
    void noPermitirModerarResenasYaModeradas() throws Exception {
        // La reseña 1 ya fue moderada previamente (está en APPROVED)
        CambiarEstadoResenaDto peticion = new CambiarEstadoResenaDto(ReviewStatus.REJECTED);

        mockMvc.perform(patch("/api/moderation/reviews/1/status")
                        .header("Authorization", "Bearer " + tokenModerador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapeadorObjetos.writeValueAsString(peticion)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("No se puede cambiar el estado de una reseña que ya fue moderada previamente"));
    }

    @Test
    @DisplayName("USER intenta editar una reseña ajena y recibe 403 Forbidden")
    void userIntentaEditarResenaAjena_debeRetornar403() throws Exception {
        // La reseña 7 pertenece a Ana Torres (id 5), Juan intenta editarla
        ActualizarResenaDto peticion = new ActualizarResenaDto(3, "Intento cambiar comentario ajeno");

        mockMvc.perform(patch("/api/reviews/7")
                        .header("Authorization", "Bearer " + tokenUsuarioJuan)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapeadorObjetos.writeValueAsString(peticion)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("No tienes permiso para editar una reseña que no es tuya"));
    }

    @Test
    @DisplayName("Reseña en estado APPROVED no puede modificarse")
    void resenaApproved_noPuedeModificarse() throws Exception {
        // La reseña 1 pertenece a Juan Pérez y está en APPROVED
        ActualizarResenaDto peticion = new ActualizarResenaDto(4, "Quiero cambiar mi reseña aprobada");

        mockMvc.perform(patch("/api/reviews/1")
                        .header("Authorization", "Bearer " + tokenUsuarioJuan)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapeadorObjetos.writeValueAsString(peticion)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Una reseña aprobada no puede ser modificada en esta versión"));
    }

    @Test
    @DisplayName("Reseña duplicada del mismo usuario para el mismo curso es rechazada con 409 Conflict")
    void resenaDuplicada_debeRetornar409() throws Exception {
        // Juan Pérez ya tiene reseña en el curso 1
        CrearResenaDto peticion = new CrearResenaDto(5, "Mi segunda reseña para el mismo curso");

        mockMvc.perform(post("/api/courses/1/reviews")
                        .header("Authorization", "Bearer " + tokenUsuarioJuan)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapeadorObjetos.writeValueAsString(peticion)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("Ya has publicado una reseña para este curso"));
    }

    @Test
    @DisplayName("Reseña REJECTED editada por su autor regresa al estado PENDING")
    void resenaRechazadaEditada_regresaAPending() throws Exception {
        // Obtener token de Ana Torres (dueña de la reseña 7 que está REJECTED)
        String tokenAna = obtenerToken("ana.torres@correo.com", "Password123!");

        ActualizarResenaDto peticion = new ActualizarResenaDto(3, "Comentario corregido y ampliado con detalles constructivos.");

        mockMvc.perform(patch("/api/reviews/7")
                        .header("Authorization", "Bearer " + tokenAna)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapeadorObjetos.writeValueAsString(peticion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.estado").value("PENDING"))
                .andExpect(jsonPath("$.comentario").value("Comentario corregido y ampliado con detalles constructivos."));
    }
}
