package com.plataforma.resenas.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:3000,http://127.0.0.1:5173}")
    private String origenesPermitidos;

    @Bean
    public PasswordEncoder codificadorContrasena() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager administradorAutenticacion(
            UserDetailsService servicioDetallesUsuario,
            PasswordEncoder codificadorContrasena) {

        DaoAuthenticationProvider proveedorAutenticacion = new DaoAuthenticationProvider();
        proveedorAutenticacion.setUserDetailsService(servicioDetallesUsuario);
        proveedorAutenticacion.setPasswordEncoder(codificadorContrasena);
        return new ProviderManager(proveedorAutenticacion);
    }

    @Bean
    public JwtAuthFilter filtroAutenticacionJwt(JwtService servicioJwt, UserDetailsService servicioDetallesUsuario) {
        return new JwtAuthFilter(servicioJwt, servicioDetallesUsuario);
    }

    @Bean
    public SecurityFilterChain cadenaFiltrosSeguridad(
            HttpSecurity http,
            JwtAuthFilter filtroAutenticacionJwt) throws Exception {

        http.cors(cors -> cors.configurationSource(fuenteConfiguracionCors()));

        http.csrf(csrf -> csrf.disable());
        http.httpBasic(basic -> basic.disable());
        http.formLogin(login -> login.disable());

        http.sessionManagement(sesion ->
                sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.exceptionHandling(excepcion -> excepcion
                .authenticationEntryPoint((solicitud, respuesta, ex) -> {
                    respuesta.setStatus(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED);
                    respuesta.setHeader("WWW-Authenticate", "Bearer");
                    respuesta.setContentType("application/json");
                    respuesta.setCharacterEncoding("UTF-8");
                    respuesta.getWriter().write("{\"error\":\"No autenticado\",\"mensaje\":\"Acceso no autorizado. Se requiere token Bearer JWT.\"}");
                })
        );

        http.authorizeHttpRequests(autorizaciones -> autorizaciones
                // Rutas públicas de autenticación
                .requestMatchers(HttpMethod.POST, "/api/auth/**").permitAll()

                // Consulta pública de cursos y de reseñas aprobadas
                .requestMatchers(HttpMethod.GET, "/api/courses").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/courses/*").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/courses/*/reviews").permitAll()

                // Rutas exclusivas del rol MODERATOR
                .requestMatchers("/api/moderation/**").hasRole("MODERATOR")

                // Rutas protegidas para usuarios con rol USER
                .requestMatchers(HttpMethod.POST, "/api/courses/*/reviews").hasRole("USER")
                .requestMatchers(HttpMethod.GET, "/api/me/reviews").hasRole("USER")
                .requestMatchers(HttpMethod.PATCH, "/api/reviews/*").hasRole("USER")

                // Documentación OpenAPI / Swagger
                .requestMatchers(
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html"
                ).permitAll()

                // Cualquier otra solicitud requiere autenticación
                .anyRequest().authenticated()
        );

        http.addFilterBefore(filtroAutenticacionJwt, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource fuenteConfiguracionCors() {
        CorsConfiguration configuracion = new CorsConfiguration();
        List<String> origenes = List.of(origenesPermitidos.split(","));
        configuracion.setAllowedOrigins(origenes);
        configuracion.setAllowedMethods(List.of("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"));
        configuracion.setAllowedHeaders(List.of("*"));
        configuracion.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/**", configuracion);
        return fuente;
    }
}
