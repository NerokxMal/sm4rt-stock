package com.malcom.sm4rtstock.exception;

// Esta excepción representa el caso "busqué algo en la BD y no lo encontré".
// Al tener su propia clase, el GlobalExceptionHandler puede identificarla
// y devolver exactamente 404 — sin afectar otros tipos de error.
//
// Extender RuntimeException significa que es una "unchecked exception":
// no tienes que declarar "throws" en cada mtodo que la lance,
// lo que hace el código más limpio.
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        // super() llama al constructor de RuntimeException con el mensaje.
        // Ese mensaje es el que aparecerá en la respuesta JSON: { "error": "..." }
        super(message);
    }
}
