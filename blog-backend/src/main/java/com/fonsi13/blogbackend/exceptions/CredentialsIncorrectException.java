package com.fonsi13.blogbackend.exceptions;

public class CredentialsIncorrectException extends RuntimeException {
    public CredentialsIncorrectException() {
        super("Credenciales incorrectas");
    }
}
