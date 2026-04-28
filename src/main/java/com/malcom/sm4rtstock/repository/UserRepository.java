package com.malcom.sm4rtstock.repository;

import com.malcom.sm4rtstock.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// JpaRepository<User, Long> le dice a Spring:
// "esta interfaz maneja la entidad User, cuya clave primaria es de tipo Long"
// Spring Data genera automáticamente el SQL de operaciones comunes:
// save(), findById(), findAll(), deleteById(), existsById()... sin que escribas nada.
//
// Es la misma idea que ya tienes en ProductoRepository y CategoriaRepository.
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Data lee el nombre del metoodo y genera el SQL correspondiente.
    // "findByUsername" se convierte en: SELECT * FROM users WHERE username = ?
    // El Optional<User> significa que puede devolver un usuario o estar vacío
    // (si no existe ese username), sin lanzar una excepción.
    // Usamos Optional en lugar de User directamente para manejar el caso
    // "usuario no encontrado" de forma elegante en el servicio.
    Optional<User> findByUsername(String username);

    // Spring Data convierte esto en: SELECT COUNT(*) > 0 FROM users WHERE username = ?
    // Lo usaremos en el registro para verificar si el username ya está tomado
    // antes de intentar guardar — más claro y eficiente que capturar una excepción.
    boolean existsByUsername(String username);
}