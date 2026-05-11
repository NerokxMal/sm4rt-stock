package com.malcom.sm4rtstock.controller;

import com.malcom.sm4rtstock.model.Role;
import com.malcom.sm4rtstock.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ─── DTOs ─────────────────────────────────────────────────────────

    @Data
    static class AuthRequest {
        @NotBlank(message = "El username es obligatorio")
        private String username;
        @NotBlank(message = "La contraseña es obligatoria")
        private String password;
    }

    @Data
    static class CreateUserRequest {
        @NotBlank(message = "El username es obligatorio")
        private String username;
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
        private String password;
        private Role role = Role.USER; // por defecto USER si no se especifica
    }

    @Data
    static class ChangePasswordRequest {
        @NotBlank(message = "La contraseña actual es obligatoria")
        private String passwordActual;
        @NotBlank(message = "La nueva contraseña es obligatoria")
        @Size(min = 6, message = "La nueva contraseña debe tener al menos 6 caracteres")
        private String passwordNuevo;
        @NotBlank(message = "Confirma la nueva contraseña")
        private String passwordConfirmar;
    }

    // ─── AUTH BÁSICA ──────────────────────────────────────────────────

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody AuthRequest request) {
        AuthService.AuthResponse response = authService.register(request.getUsername(), request.getPassword());
        return ResponseEntity.status(201).body(Map.of("token", response.token(), "role", response.role()));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody AuthRequest request) {
        AuthService.AuthResponse response = authService.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(Map.of("token", response.token(), "role", response.role()));
    }

    // ─── GESTIÓN DE USUARIOS (solo ADMIN) ────────────────────────────

    // GET /auth/users → lista todos los usuarios sin contraseñas
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuthService.UsuarioDTO>> listarUsuarios() {
        return ResponseEntity.ok(authService.listarUsuarios());
    }

    // POST /auth/users → crear usuario con rol asignable
    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> crearUsuario(@Valid @RequestBody CreateUserRequest request) {
        AuthService.AuthResponse response = authService.registerConRol(
                request.getUsername(),
                request.getPassword(),
                request.getRole() != null ? request.getRole() : Role.USER
        );
        return ResponseEntity.status(201).body(Map.of(
                "message", "Usuario creado correctamente",
                "username", request.getUsername(),
                "role", response.role()
        ));
    }

    // PUT /auth/users/{username}/role → cambiar rol
    @PutMapping("/users/{username}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> cambiarRol(
            @PathVariable String username,
            @RequestParam Role role) {
        authService.cambiarRol(username, role);
        return ResponseEntity.ok(Map.of("message", "Rol actualizado a " + role.name()));
    }

    // PUT /auth/users/{username}/status → activar / desactivar cuenta
    @PutMapping("/users/{username}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> toggleEstado(@PathVariable String username) {
        boolean nuevoEstado = authService.toggleEstado(username);
        return ResponseEntity.ok(Map.of(
                "message", nuevoEstado ? "Cuenta activada" : "Cuenta desactivada",
                "enabled", String.valueOf(nuevoEstado)
        ));
    }

    // ─── CONTRASEÑA PROPIA (usuario autenticado) ──────────────────────

    // PUT /auth/password → el usuario autenticado cambia su propia contraseña.
    // Authentication lo inyecta Spring Security automáticamente — es el usuario
    // que está autenticado en el request actual, no uno pasado en el body.
    @PutMapping("/password")
    public ResponseEntity<Map<String, String>> cambiarPassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {

        // Validar que las dos contraseñas nuevas coincidan antes de llamar al servicio
        if (!request.getPasswordNuevo().equals(request.getPasswordConfirmar())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Las contraseñas nuevas no coinciden"));
        }

        authService.cambiarPassword(
                authentication.getName(), // username del token JWT actual
                request.getPasswordActual(),
                request.getPasswordNuevo()
        );

        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada correctamente"));
    }
}