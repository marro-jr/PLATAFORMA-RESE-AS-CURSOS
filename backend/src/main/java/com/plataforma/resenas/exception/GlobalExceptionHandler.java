package com.plataforma.resenas.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorRespuestaDto> manejarRecursoNoEncontrado(
            RecursoNoEncontradoException excepcion,
            HttpServletRequest solicitud) {

        ErrorRespuestaDto respuesta = new ErrorRespuestaDto(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                excepcion.getMessage(),
                solicitud.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(respuesta);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorRespuestaDto> manejarReglaNegocio(
            ReglaNegocioException excepcion,
            HttpServletRequest solicitud) {

        HttpStatus estado = excepcion.getCodigoEstado() != null ? excepcion.getCodigoEstado() : HttpStatus.BAD_REQUEST;
        ErrorRespuestaDto respuesta = new ErrorRespuestaDto(
                estado.value(),
                estado.getReasonPhrase(),
                excepcion.getMessage(),
                solicitud.getRequestURI()
        );
        return ResponseEntity.status(estado).body(respuesta);
    }

    @ExceptionHandler({AccesoDenegadoException.class, AccessDeniedException.class})
    public ResponseEntity<ErrorRespuestaDto> manejarAccesoDenegado(
            Exception excepcion,
            HttpServletRequest solicitud) {

        ErrorRespuestaDto respuesta = new ErrorRespuestaDto(
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                excepcion.getMessage() != null ? excepcion.getMessage() : "Acceso denegado a este recurso",
                solicitud.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(respuesta);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorRespuestaDto> manejarCredencialesInvalidas(
            BadCredentialsException excepcion,
            HttpServletRequest solicitud) {

        ErrorRespuestaDto respuesta = new ErrorRespuestaDto(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "Credenciales inválidas. Verifique su correo y contraseña.",
                solicitud.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(respuesta);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuestaDto> manejarValidacionArgumentos(
            MethodArgumentNotValidException excepcion,
            HttpServletRequest solicitud) {

        Map<String, String> errores = new HashMap<>();
        for (FieldError error : excepcion.getBindingResult().getFieldErrors()) {
            errores.put(error.getField(), error.getDefaultMessage());
        }

        ErrorRespuestaDto respuesta = new ErrorRespuestaDto(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Error en la validación de los datos enviados",
                solicitud.getRequestURI(),
                errores
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRespuestaDto> manejarExcepcionGeneral(
            Exception excepcion,
            HttpServletRequest solicitud) {

        ErrorRespuestaDto respuesta = new ErrorRespuestaDto(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                excepcion.getMessage() != null ? excepcion.getMessage() : "Error interno del servidor",
                solicitud.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(respuesta);
    }
}
