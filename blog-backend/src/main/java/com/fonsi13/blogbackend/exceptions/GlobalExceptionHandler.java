package com.fonsi13.blogbackend.exceptions;

import com.fonsi13.blogbackend.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice //Maneja los errores de TODOS los controladores aquí
public class GlobalExceptionHandler {

    //Cuando lanzamos nuestra ResourceNotFoundException
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    // Cuando el usuario no tiene permisos para realizar una acción
    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ApiResponse<Object>> handleUnauthorizedAccessException(UnauthorizedAccessException ex) {
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.FORBIDDEN);
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

    // Cuando el archivo excede el tamaño máximo configurado en Spring (5MB)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Object>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        log.warn("Intento de subida de archivo excediendo el tamaño máximo: {}", ex.getMessage());
        return new ResponseEntity<>(
                ApiResponse.error("El archivo excede el tamaño máximo permitido (5MB)"),
                HttpStatus.BAD_REQUEST
        );
    }

    // Cualquier otro error no controlado (RuntimeException genérica)
    // Se loggea el detalle internamente pero NO se expone al cliente (OWASP A01:2021)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGlobalException(Exception ex) {
        log.error("Error interno no controlado: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(
                ApiResponse.error("Error interno del servidor. Intente nuevamente más tarde."),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
