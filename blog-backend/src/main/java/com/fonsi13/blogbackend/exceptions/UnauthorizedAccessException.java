package com.fonsi13.blogbackend.exceptions;

public class UnauthorizedAccessException extends RuntimeException {

    public UnauthorizedAccessException(String message) {
        super(message);
    }

    public UnauthorizedAccessException(String resourceName, String action) {
        super(String.format("No tienes permisos para %s este %s", action, resourceName));
    }
}
