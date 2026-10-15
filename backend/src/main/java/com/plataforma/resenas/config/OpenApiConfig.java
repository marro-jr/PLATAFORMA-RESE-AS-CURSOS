package com.plataforma.resenas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    private static final String ESQUEMA_SEGURIDAD = "BearerAuth";

    @Bean
    public OpenAPI configuracionOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Plataforma de Reseñas de Cursos")
                        .version("1.0.0")
                        .description("API REST para consulta de cursos, publicación de reseñas y flujo de moderación con Spring Security y JWT.")
                        .contact(new Contact()
                                .name("Portafolio de Prácticas Preprofesionales")
                                .email("contacto@plataforma.com")))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_SEGURIDAD))
                .components(new Components()
                        .addSecuritySchemes(ESQUEMA_SEGURIDAD, new SecurityScheme()
                                .name(ESQUEMA_SEGURIDAD)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
