package com.fonsi13.blogbackend.exceptions;

import com.fonsi13.blogbackend.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice //Maneja los errores de TODOS los controladores aquí
public class GlobalExceptionHandler {

    //Cuando lanzamos nuestra ResourceNotFoundException
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    //  Cuando fallan las validaciones (@NotBlank, @Email, etc.)
    // Esto es vital para que el frontend sepa qué campo llenó mal
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        // Devolvemos el mapa de errores dentro del "data" o concatenado en el mensaje
        return new ResponseEntity<>(
                ApiResponse.error("Error de validación: " + errors.toString()),
                HttpStatus.BAD_REQUEST
        );
    }

    // Cualquier otro error no controlado (RuntimeException genérica)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGlobalException(Exception ex) {
        return new ResponseEntity<>(
                ApiResponse.error("Error interno del servidor: " + ex.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
