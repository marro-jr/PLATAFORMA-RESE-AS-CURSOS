package com.plataforma.resenas.exception;

import java.time.Instant;
import java.util.Map;

public record ErrorRespuestaDto(
        Instant marcaTiempo,
        int codigoEstado,
        String error,
        String mensaje,
        String ruta,
        Map<String, String> detalles
) {
    public ErrorRespuestaDto(int codigoEstado, String error, String mensaje, String ruta) {
        this(Instant.now(), codigoEstado, error, mensaje, ruta, null);
    }

    public ErrorRespuestaDto(int codigoEstado, String error, String mensaje, String ruta, Map<String, String> detalles) {
        this(Instant.now(), codigoEstado, error, mensaje, ruta, detalles);
    }
}
