package com.malcom.sm4rtstock.exception;

// Esta excepción representa el caso "ya existe un recurso con esos datos".
// Por ejemplo: intentar registrar un username que ya está en uso,
// o crear una categoría con un nombre que ya existe.
// HTTP 409 Conflict es el código correcto para este escenario.
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
