package com.plataforma.resenas.exception;

import org.springframework.http.HttpStatus;

public class ReglaNegocioException extends RuntimeException {

    private final HttpStatus codigoEstado;

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
        this.codigoEstado = HttpStatus.BAD_REQUEST;
    }

    public ReglaNegocioException(String mensaje, HttpStatus codigoEstado) {
        super(mensaje);
        this.codigoEstado = codigoEstado;
    }

    public HttpStatus getCodigoEstado() {
        return codigoEstado;
    }
}
