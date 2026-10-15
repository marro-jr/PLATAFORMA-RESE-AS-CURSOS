package com.plataforma.resenas.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.plataforma.resenas.dto.LoginPeticionDto;
import com.plataforma.resenas.dto.LoginRespuestaDto;
import com.plataforma.resenas.dto.RegistroPeticionDto;
import com.plataforma.resenas.dto.UsuarioRespuestaDto;
import com.plataforma.resenas.entity.AppUser;
import com.plataforma.resenas.entity.Role;
import com.plataforma.resenas.exception.ReglaNegocioException;
import com.plataforma.resenas.repository.AppUserRepository;
import com.plataforma.resenas.security.JwtService;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder codificadorContrasena;
    private final AuthenticationManager administradorAutenticacion;
    private final JwtService servicioJwt;

    public AuthService(
            AppUserRepository appUserRepository,
            PasswordEncoder codificadorContrasena,
            AuthenticationManager administradorAutenticacion,
            JwtService servicioJwt) {

        this.appUserRepository = appUserRepository;
        this.codificadorContrasena = codificadorContrasena;
        this.administradorAutenticacion = administradorAutenticacion;
        this.servicioJwt = servicioJwt;
    }

    @Transactional
    public UsuarioRespuestaDto registrarUsuario(RegistroPeticionDto peticion) {
        String correoLimpio = peticion.correo().trim().toLowerCase();

        if (appUserRepository.existsByEmail(correoLimpio)) {
            throw new ReglaNegocioException("El correo ya está registrado en la plataforma", HttpStatus.CONFLICT);
        }

        // Regla estricta: El registro público SIEMPRE crea rol USER, ignorando cualquier intento de autoasignarse MODERATOR
        Role rolAsignado = Role.USER;

        String contrasenaHasheada = codificadorContrasena.encode(peticion.clave());

        AppUser nuevoUsuario = new AppUser(
                peticion.nombre().trim(),
                correoLimpio,
                contrasenaHasheada,
                rolAsignado
        );

        AppUser usuarioGuardado = appUserRepository.save(nuevoUsuario);

        return new UsuarioRespuestaDto(
                usuarioGuardado.getId(),
                usuarioGuardado.getName(),
                usuarioGuardado.getEmail(),
                usuarioGuardado.getRole(),
                usuarioGuardado.isActive()
        );
    }

    @Transactional(readOnly = true)
    public LoginRespuestaDto autenticarUsuario(LoginPeticionDto peticion) {
        String correoLimpio = peticion.correo().trim().toLowerCase();

        try {
            Authentication credenciales = UsernamePasswordAuthenticationToken.unauthenticated(
                    correoLimpio,
                    peticion.clave()
            );

            Authentication autenticacion = administradorAutenticacion.authenticate(credenciales);

            AppUser usuario = appUserRepository.findByEmail(autenticacion.getName())
                    .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

            String token = servicioJwt.generarToken(
                    usuario.getEmail(),
                    usuario.getRole().name(),
                    usuario.getName()
            );

            return new LoginRespuestaDto(
                    token,
                    "Bearer",
                    usuario.getRole().name(),
                    usuario.getEmail(),
                    usuario.getName()
            );

        } catch (AuthenticationException excepcion) {
            throw new BadCredentialsException("Credenciales inválidas");
        }
    }
}
