package com.plataforma.resenas.security;

import java.time.Instant;
import java.util.Date;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final SecretKey clave;
    private final long duracionSegundos;

    public JwtService(
            @Value("${app.jwt.secret}") String textoBase64,
            @Value("${app.jwt.expiration-seconds:86400}") long duracionSegundos) {

        if (textoBase64 == null || textoBase64.trim().isEmpty()) {
            throw new IllegalArgumentException("La clave secreta JWT (app.jwt.secret / JWT_SECRET_BASE64) es obligatoria y no puede ser nula ni vacía.");
        }

        byte[] bytes;
        try {
            bytes = Decoders.BASE64.decode(textoBase64.trim());
        } catch (Exception excepcion) {
            throw new IllegalArgumentException("La clave secreta JWT no tiene una codificación Base64 válida.", excepcion);
        }

        if (bytes.length < 32) {
            throw new IllegalArgumentException("La clave secreta JWT debe tener una longitud mínima de 256 bits (32 bytes) para garantizar la seguridad con HMAC-SHA256.");
        }

        this.clave = Keys.hmacShaKeyFor(bytes);
        this.duracionSegundos = duracionSegundos;
    }

    public String generarToken(String correo, String rol, String nombre) {
        Instant ahora = Instant.now();
        Instant vencimiento = ahora.plusSeconds(this.duracionSegundos);

        return Jwts.builder()
                .subject(correo)
                .claim("rol", rol)
                .claim("nombre", nombre)
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(vencimiento))
                .signWith(this.clave, Jwts.SIG.HS256)
                .compact();
    }

    public String obtenerCorreo(String token) {
        return obtenerReclamo(token, Claims::getSubject);
    }

    public String obtenerRol(String token) {
        return obtenerReclamo(token, claims -> claims.get("rol", String.class));
    }

    public String obtenerNombre(String token) {
        return obtenerReclamo(token, claims -> claims.get("nombre", String.class));
    }

    public Date obtenerFechaExpiracion(String token) {
        return obtenerReclamo(token, Claims::getExpiration);
    }

    public boolean esTokenValido(String token, UserDetails usuario) {
        try {
            String correo = obtenerCorreo(token);
            return (correo != null && correo.equalsIgnoreCase(usuario.getUsername()) && !esTokenExpirado(token));
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public boolean esTokenExpirado(String token) {
        Date expiracion = obtenerFechaExpiracion(token);
        return expiracion.before(new Date());
    }

    public <T> T obtenerReclamo(String token, Function<Claims, T> resolvedorReclamos) {
        Claims reclamos = obtenerTodosLosReclamos(token);
        return resolvedorReclamos.apply(reclamos);
    }

    private Claims obtenerTodosLosReclamos(String token) {
        return Jwts.parser()
                .verifyWith(this.clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
