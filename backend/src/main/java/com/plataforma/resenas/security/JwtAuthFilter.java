package com.plataforma.resenas.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest solicitud) {
        String ruta = solicitud.getServletPath();
        String metodo = solicitud.getMethod();
        return "POST".equalsIgnoreCase(metodo) && ruta.startsWith("/api/auth/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest solicitud,
            HttpServletResponse respuesta,
            FilterChain cadenaFiltros) throws ServletException, IOException {

        String cabecera = solicitud.getHeader("Authorization");

        if (cabecera == null || !cabecera.regionMatches(true, 0, "Bearer ", 0, 7)) {
            cadenaFiltros.doFilter(solicitud, respuesta);
            return;
        }

        try {
            String token = cabecera.substring(7).trim();
            String correo = jwtService.obtenerCorreo(token);

            if (correo == null || correo.isBlank()) {
                responderNoAutorizado(respuesta, "Token JWT sin identificador de usuario válido");
                return;
            }

            UserDetails usuario = userDetailsService.loadUserByUsername(correo);

            if (!usuario.isEnabled()
                    || !usuario.isAccountNonLocked()
                    || !usuario.isAccountNonExpired()
                    || !usuario.isCredentialsNonExpired()) {
                responderNoAutorizado(respuesta, "La cuenta de usuario se encuentra inactiva o bloqueada");
                return;
            }

            UsernamePasswordAuthenticationToken autenticacion =
                    UsernamePasswordAuthenticationToken.authenticated(
                            usuario,
                            null,
                            usuario.getAuthorities()
                    );

            SecurityContext contexto = SecurityContextHolder.createEmptyContext();
            contexto.setAuthentication(autenticacion);
            SecurityContextHolder.setContext(contexto);

        } catch (JwtException | UsernameNotFoundException | IllegalArgumentException excepcion) {
            responderNoAutorizado(respuesta, "Token JWT ausente, alterado o vencido");
            return;
        }

        cadenaFiltros.doFilter(solicitud, respuesta);
    }

    private void responderNoAutorizado(HttpServletResponse respuesta, String mensajeError) throws IOException {
        SecurityContextHolder.clearContext();
        respuesta.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        respuesta.setHeader("WWW-Authenticate", "Bearer");
        respuesta.setContentType("application/json");
        respuesta.setCharacterEncoding("UTF-8");
        respuesta.getWriter().write(String.format("{\"error\":\"No autenticado\",\"mensaje\":\"%s\"}", mensajeError));
    }
}
