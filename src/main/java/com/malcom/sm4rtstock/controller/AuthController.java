package com.malcom.sm4rtstock.controller;

import com.malcom.sm4rtstock.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
// @RestController = @Controller + @ResponseBody
// Le dice a Spring que esta clase maneja peticiones HTTP y que
// cada metodo devuelve datos (JSON) directamente, no una vista HTML.
@RequestMapping("/auth")
// Todas las rutas de este controlador empezarán con /auth:
// /auth/register, /auth/login, etc.
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ─── DTOs (Data Transfer Objects) ─────────────────────────────────
    //
    // Un DTO es una clase simple que representa los datos que esperamos
    // recibir en el body del request. En lugar de recibir parámetros sueltos,
    // Spring deserializa el JSON automáticamente a este objeto.
    //
    // Por ejemplo, si el cliente envía:
    //   { "username": "malcom", "password": "1234" }
    // Spring lo convierte en un objeto AuthRequest con esos valores.
    //
    // Los definimos como clases internas estáticas para mantenerlos
    // cerca del controlador que los usa, sin crear archivos separados.
    // En proyectos más grandes tendrían su propio paquete dto/.

    @Data // Lombok genera getters y setters — necesarios para que Spring pueda leer los campos
    static class AuthRequest {
        @NotBlank(message = "El username es obligatorio")
        private String username;

        @NotBlank(message = "La contraseña es obligatoria")
        private String password;
    }

    // ─── REGISTRO ─────────────────────────────────────────────────────

    @PostMapping("/register")
    // @Valid activa las validaciones de @NotBlank que pusimos en AuthRequest.
    // Si el JSON llega sin username o password, Spring devuelve 400 Bad Request
    // automáticamente antes de que el metodo se ejecute.
    // @RequestBody le dice a Spring que el objeto viene del body del request en formato JSON.
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody AuthRequest request) {
        String token = authService.register(request.getUsername(), request.getPassword());

        // ResponseEntity nos permite controlar el código HTTP de la respuesta.
        // 201 Created es el código correcto cuando se crea un recurso nuevo.
        // Map.of() crea un mapa simple { "token": "eyJ..." } como respuesta JSON.
        return ResponseEntity.status(201).body(Map.of("token", token));
    }

    // ─── LOGIN ────────────────────────────────────────────────────────

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody AuthRequest request) {
        String token = authService.login(request.getUsername(), request.getPassword());

        // 200 OK es el código correcto para un login exitoso.
        return ResponseEntity.ok(Map.of("token", token));
    }
}