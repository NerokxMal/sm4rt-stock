package com.malcom.sm4rtstock.service;

import com.malcom.sm4rtstock.exception.ConflictException;
import com.malcom.sm4rtstock.exception.ResourceNotFoundException;
import com.malcom.sm4rtstock.model.Permission;
import com.malcom.sm4rtstock.model.Role;
import com.malcom.sm4rtstock.model.User;
import com.malcom.sm4rtstock.repository.UserRepository;
import com.malcom.sm4rtstock.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String USER_DETAILS_CACHE = "usersByUsername";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuditoriaService auditoriaService;
    private final CacheManager cacheManager;

    // ─── Record de respuesta auth ─────────────────────────────────────
    public record AuthResponse(String token, String role, Set<Permission> permissions) {}

    // ─── Record para exponer datos de usuario sin la contraseña ───────
    // Nunca enviamos el hash de contraseña al frontend.
    // Este record es lo que devuelve GET /auth/users.
    public record UsuarioDTO(Long id, String username, String role, boolean enabled, Set<Permission> permissions) {}

    // ─── REGISTRO ─────────────────────────────────────────────────────
    public AuthResponse register(String username, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new ConflictException("El nombre de usuario ya está en uso: " + username);
        }
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .permissions(Permission.defaultsForRole(Role.USER))
                .build();
        userRepository.save(user);
        evictUserCache(user.getUsername());
        auditoriaService.registrar(
                "CREAR",
                "USUARIO",
                user.getId(),
                "Registro de nuevo usuario: " + user.getUsername()
        );
        return new AuthResponse(
                jwtTokenProvider.generateToken(user),
                user.getRole().name(),
                copiarPermisos(user.getPermissions())
        );
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
                .permissions(Permission.defaultsForRole(role))
                .build();
        userRepository.save(user);
        evictUserCache(user.getUsername());
        auditoriaService.registrar(
                "CREAR",
                "USUARIO",
                user.getId(),
                "Usuario creado por administrador: " + user.getUsername() + " [" + role.name() + "]"
        );
        return new AuthResponse(
                jwtTokenProvider.generateToken(user),
                user.getRole().name(),
                copiarPermisos(user.getPermissions())
        );
    }

    // ─── LOGIN ────────────────────────────────────────────────────────
    public AuthResponse login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("Credenciales incorrectas"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Credenciales incorrectas");
        }
        normalizarPermisosSiFaltan(user);
        return new AuthResponse(
                jwtTokenProvider.generateToken(user),
                user.getRole().name(),
                copiarPermisos(user.getPermissions())
        );
    }

    // ─── LISTAR USUARIOS ──────────────────────────────────────────────
    // Devuelve todos los usuarios sin exponer contraseñas.
    // Solo ADMIN puede llamar esto.
    public List<UsuarioDTO> listarUsuarios() {
        return userRepository.findAll().stream()
                .peek(this::normalizarPermisosSiFaltan)
                .map(u -> new UsuarioDTO(
                        u.getId(),
                        u.getUsername(),
                        u.getRole().name(),
                        u.isEnabled(),
                        copiarPermisos(u.getPermissions())
                ))
                .toList();
    }

    // ─── CAMBIAR ROL ──────────────────────────────────────────────────
    public void cambiarRol(String username, Role role) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
        user.setRole(role);
        user.setPermissions(Permission.defaultsForRole(role));
        userRepository.save(user);
        evictUserCache(user.getUsername());
        auditoriaService.registrar(
                "CAMBIAR_ROL",
                "USUARIO",
                user.getId(),
                "Rol actualizado para " + user.getUsername() + ": " + role.name()
        );
    }

    public void actualizarPermisos(String username, Set<Permission> permisos) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
        user.setPermissions(permisos == null || permisos.isEmpty()
                ? EnumSet.noneOf(Permission.class)
                : EnumSet.copyOf(permisos));
        userRepository.save(user);
        evictUserCache(user.getUsername());
        auditoriaService.registrar(
                "ACTUALIZAR_PERMISOS",
                "USUARIO",
                user.getId(),
                "Permisos actualizados para " + user.getUsername()
        );
    }

    // ─── TOGGLE ESTADO (activar / desactivar) ─────────────────────────
    // No borramos usuarios — los desactivamos. Eso preserva el historial
    // de movimientos que referencia a ese usuario.
    public boolean toggleEstado(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
        evictUserCache(user.getUsername());
        auditoriaService.registrar(
                "CAMBIAR_ESTADO",
                "USUARIO",
                user.getId(),
                "Cuenta " + (user.isEnabled() ? "activada" : "desactivada") + ": " + user.getUsername()
        );
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
        evictUserCache(user.getUsername());
        auditoriaService.registrar(
                "CAMBIAR_PASSWORD",
                "USUARIO",
                user.getId(),
                "Cambio de contraseña para el usuario " + user.getUsername()
        );
    }

    public UsuarioDTO obtenerUsuarioPorUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
        normalizarPermisosSiFaltan(user);
        return new UsuarioDTO(
                user.getId(),
                user.getUsername(),
                user.getRole().name(),
                user.isEnabled(),
                copiarPermisos(user.getPermissions())
        );
    }

    public List<Permission> listarPermisosDisponibles() {
        return List.of(Permission.values());
    }

    private void normalizarPermisosSiFaltan(User user) {
        if (user.getPermissions() == null || user.getPermissions().isEmpty()) {
            user.setPermissions(Permission.defaultsForRole(user.getRole()));
            userRepository.save(user);
            evictUserCache(user.getUsername());
        }
    }

    private void evictUserCache(String username) {
        if (username == null || username.isBlank()) {
            return;
        }
        Cache cache = cacheManager.getCache(USER_DETAILS_CACHE);
        if (cache != null) {
            cache.evict(username);
        }
    }

    private Set<Permission> copiarPermisos(Set<Permission> permisos) {
        if (permisos == null || permisos.isEmpty()) {
            return EnumSet.noneOf(Permission.class);
        }
        return EnumSet.copyOf(permisos);
    }
}
