package com.malcom.sm4rtstock.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

// @RestControllerAdvice convierte esta clase en un "interceptor global de errores".
// Cuando cualquier controlador lanza una excepción, Spring la atrapa aquí
// en lugar de dejar que el servidor devuelva un error genérico 500.
// Es como un try-catch centralizado para toda la aplicación.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ─── 404 NOT FOUND ────────────────────────────────────────────────
    // Se lanza cuando buscamos algo en la BD y no existe.
    // Ejemplos: obtenerPorId() con un id que no existe.
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)           // 404
                .body(Map.of("error", ex.getMessage()));
    }

    // ─── 409 CONFLICT ────────────────────────────────────────────────
    // Se lanza cuando intentamos crear algo que ya existe.
    // Ejemplos: username duplicado, nombre de categoría duplicado.
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, String>> handleConflict(ConflictException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)            // 409
                .body(Map.of("error", ex.getMessage()));
    }

    // ─── 401 UNAUTHORIZED ─────────────────────────────────────────────
    // Se lanza cuando las credenciales de login son incorrectas.
    // BadCredentialsException viene de Spring Security — la lanzamos
    // nosotros en AuthService cuando el password no coincide.
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)        // 401
                .body(Map.of("error", "Credenciales incorrectas"));
        // Nota: no usamos ex.getMessage() aquí intencionalmente.
        // Siempre devolvemos el mismo mensaje genérico para no revelar
        // si el problema fue el username o la contraseña.
    }

    // ─── 400 BAD REQUEST — Validaciones de campos ─────────────────────
    // Se lanza cuando @Valid encuentra que el JSON enviado no cumple
    // las restricciones (@NotBlank, @Min, @Size, etc.).
    // Devolvemos un mapa con cada campo que falló y su mensaje de error.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();

        // getBindingResult().getFieldErrors() devuelve la lista de campos
        // que fallaron la validación. Por ejemplo si mandas precio negativo
        // y nombre vacío, obtendrás dos entradas en el mapa:
        // { "precio": "debe ser mayor que 0", "nombre": "El nombre es obligatorio" }
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errores.put(error.getField(), error.getDefaultMessage())
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)         // 400
                .body(errores);
    }

    // ─── 500 INTERNAL SERVER ERROR — Fallback ─────────────────────────
    // Captura cualquier excepción que no hayamos contemplado arriba.
    // Es el "último recurso" — preferimos que llegue aquí lo menos posible.
    // En producción, este handler evita que el servidor exponga
    // stack traces con información interna al cliente.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneral(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)  // 500
                .body(Map.of("error", "Error interno del servidor"));
    }
}
