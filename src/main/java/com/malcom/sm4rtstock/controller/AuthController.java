package com.malcom.sm4rtstock.controller;

import com.malcom.sm4rtstock.model.Role;
import com.malcom.sm4rtstock.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ─── DTO de request ───────────────────────────────────────────────
    // Misma clase de antes — recibe username y password del body JSON

    @Data
    static class AuthRequest {
        @NotBlank(message = "El username es obligatorio")
        private String username;

        @NotBlank(message = "La contraseña es obligatoria")
        private String password;
    }

    // ─── REGISTRO ─────────────────────────────────────────────────────

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody AuthRequest request) {
        // authService.register() ahora devuelve AuthResponse con token + role
        AuthService.AuthResponse response = authService.register(
                request.getUsername(),
                request.getPassword()
        );

        return ResponseEntity.status(201).body(Map.of(
                "token", response.token(),
                "role",  response.role()
        ));
    }

    // ─── LOGIN ────────────────────────────────────────────────────────

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody AuthRequest request) {
        AuthService.AuthResponse response = authService.login(
                request.getUsername(),
                request.getPassword()
        );

        return ResponseEntity.ok(Map.of(
                "token", response.token(),
                "role",  response.role()
        ));
    }

    // ─── CAMBIAR ROL ──────────────────────────────────────────────────
    // Solo accesible para usuarios con rol ADMIN
    // @PreAuthorize requiere @EnableMethodSecurity en SecurityConfig

    @PutMapping("/users/{username}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> cambiarRol(
            @PathVariable String username,
            @RequestParam Role role) {

        authService.cambiarRol(username, role);
        return ResponseEntity.ok(Map.of("message", "Rol actualizado a " + role.name()));
    }
}