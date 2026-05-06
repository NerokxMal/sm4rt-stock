package com.malcom.sm4rtstock.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

// @Entity le dice a Hibernate que esta clase representa una tabla en la base de datos.
// Igual que Producto o Categoria — Hibernate leerá los campos y creará la tabla "users"
// automáticamente gracias a ddl-auto=update en application.properties.
@Entity
@Table(name = "users")
// Las siguientes anotaciones son de Lombok — generan código repetitivo por ti:
// @Data genera getters, setters, equals, hashCode y toString automáticamente.
// @Builder permite construir objetos así: User.builder().username("x").build()
// @NoArgsConstructor y @AllArgsConstructor generan los constructores vacío y completo.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// UserDetails es una interfaz de Spring Security.
// Al implementarla, Spring sabrá cómo obtener el nombre de usuario, contraseña
// y permisos de este objeto — sin necesidad de configuración extra.
public class User implements UserDetails {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.USER;  // por defecto toodo usuario nuevo es USER

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // GenerationType.IDENTITY le dice a MySQL que use AUTO_INCREMENT.
    // Cada usuario nuevo recibirá un id único automáticamente.
    private Long id;

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Column(nullable = false, unique = true)
    // unique = true le dice a la base de datos que no puede haber
    // dos usuarios con el mismo nombre. Si alguien intenta registrarse
    // con un username ya existente, la BD lanzará un error.
    private String username;

    @NotBlank(message = "La contraseña es obligatoria")
    @Column(nullable = false)
    // Aquí guardaremos la contraseña ya hasheada con BCrypt,
    // NUNCA en texto plano. BCrypt convierte "miPassword123"
    // en algo como "$2a$10$X8vQ..." que es imposible de revertir.
    private String password;

    // ─── Métodos de UserDetails ───────────────────────────────────────
    // Spring Security llama a estos métodos para tomar decisiones de seguridad.
    // Tenemos que implementarlos porque firmamos el contrato UserDetails.

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
        // Genera "ROLE_ADMIN" o "ROLE_USER"
        // Spring Security requiere el prefijo ROLE_ para hasRole()
    }


    @Override
    public boolean isAccountNonExpired() {
        // ¿La cuenta está expirada? false = sí está expirada, no puede entrar.
        // Devolvemos true siempre = las cuentas nunca expiran.
        // Útil si en el futuro quieres cuentas temporales o de prueba.
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // ¿La cuenta está bloqueada? Por ejemplo, tras 5 intentos fallidos.
        // Por ahora siempre devolvemos true = nunca bloqueada.
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // ¿Las credenciales (contraseña) están expiradas?
        // Útil para sistemas que obligan a cambiar contraseña cada X días.
        // Por ahora siempre true = las contraseñas no expiran.
        return true;
    }

    @Override
    public boolean isEnabled() {
        // ¿La cuenta está activa? Si devolvieras false, el usuario
        // no podría iniciar sesión aunque su contraseña sea correcta.
        // Útil para verificación de email, suspensión de cuentas, etc.
        return true;
    }
}