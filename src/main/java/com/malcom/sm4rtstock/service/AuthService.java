package com.malcom.sm4rtstock.service;

import com.malcom.sm4rtstock.exception.ConflictException;
import com.malcom.sm4rtstock.exception.ResourceNotFoundException;
import com.malcom.sm4rtstock.model.Role;
import com.malcom.sm4rtstock.model.User;
import com.malcom.sm4rtstock.repository.UserRepository;
import com.malcom.sm4rtstock.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // ─── Record de respuesta ──────────────────────────────────────────
    // Un record en Java es una clase inmutable con getters automáticos.
    // token() y role() son los getters — Java los genera solo.
    // Lo definimos aquí adentro del servicio porque solo lo usan
    // AuthService y AuthController.

    public record AuthResponse(String token, String role) {}

    // ─── REGISTRO ─────────────────────────────────────────────────────

    public AuthResponse register(String username, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new ConflictException("El nombre de usuario ya está en uso: " + username);
        }

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                // .role() no se pone — el @Builder.Default en User lo setea en Role.USER
                .build();

        userRepository.save(user);

        // generateToken ahora recibe el User completo (para leer el rol)
        String token = jwtTokenProvider.generateToken(user);

        return new AuthResponse(token, user.getRole().name());
    }

    // ─── LOGIN ────────────────────────────────────────────────────────

    public AuthResponse login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("Credenciales incorrectas"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Credenciales incorrectas");
        }

        String token = jwtTokenProvider.generateToken(user);

        return new AuthResponse(token, user.getRole().name());
    }

    // ─── CAMBIAR ROL ──────────────────────────────────────────────────

    public void cambiarRol(String username, Role role) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado: " + username));
        user.setRole(role);
        userRepository.save(user);
    }
}