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

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // ─── Record de respuesta auth ─────────────────────────────────────
    public record AuthResponse(String token, String role) {}

    // ─── Record para exponer datos de usuario sin la contraseña ───────
    // Nunca enviamos el hash de contraseña al frontend.
    // Este record es lo que devuelve GET /auth/users.
    public record UsuarioDTO(Long id, String username, String role, boolean enabled) {}

    // ─── REGISTRO ─────────────────────────────────────────────────────
    public AuthResponse register(String username, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new ConflictException("El nombre de usuario ya está en uso: " + username);
        }
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .build();
        userRepository.save(user);
        return new AuthResponse(jwtTokenProvider.generateToken(user), user.getRole().name());
    }

    // ─── REGISTRO CON ROL ESPECÍFICO (para crear desde configuración) ─
    // Igual que register() pero permite asignar el rol en la creación.
    // Solo accesible desde el endpoint protegido con @PreAuthorize("hasRole('ADMIN')")
    public AuthResponse registerConRol(String username, String password, Role role) {
        if (userRepository.existsByUsername(username)) {
            throw new ConflictException("El nombre de usuario ya está en uso: " + username);
        }
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .role(role)
                .build();
        userRepository.save(user);
        return new AuthResponse(jwtTokenProvider.generateToken(user), user.getRole().name());
    }

    // ─── LOGIN ────────────────────────────────────────────────────────
    public AuthResponse login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("Credenciales incorrectas"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Credenciales incorrectas");
        }
        return new AuthResponse(jwtTokenProvider.generateToken(user), user.getRole().name());
    }

    // ─── LISTAR USUARIOS ──────────────────────────────────────────────
    // Devuelve todos los usuarios sin exponer contraseñas.
    // Solo ADMIN puede llamar esto.
    public List<UsuarioDTO> listarUsuarios() {
        return userRepository.findAll().stream()
                .map(u -> new UsuarioDTO(u.getId(), u.getUsername(), u.getRole().name(), u.isEnabled()))
                .toList();
    }

    // ─── CAMBIAR ROL ──────────────────────────────────────────────────
    public void cambiarRol(String username, Role role) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
        user.setRole(role);
        userRepository.save(user);
    }

    // ─── TOGGLE ESTADO (activar / desactivar) ─────────────────────────
    // No borramos usuarios — los desactivamos. Eso preserva el historial
    // de movimientos que referencia a ese usuario.
    public boolean toggleEstado(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
        return user.isEnabled(); // devuelve el nuevo estado
    }

    // ─── CAMBIAR CONTRASEÑA PROPIA ────────────────────────────────────
    // El usuario solo puede cambiar su propia contraseña.
    // Verificamos la contraseña actual antes de aceptar la nueva.
    public void cambiarPassword(String username, String passwordActual, String passwordNuevo) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));

        // Verificar que la contraseña actual sea correcta
        if (!passwordEncoder.matches(passwordActual, user.getPassword())) {
            throw new BadCredentialsException("La contraseña actual es incorrecta");
        }

        user.setPassword(passwordEncoder.encode(passwordNuevo));
        userRepository.save(user);
    }
}