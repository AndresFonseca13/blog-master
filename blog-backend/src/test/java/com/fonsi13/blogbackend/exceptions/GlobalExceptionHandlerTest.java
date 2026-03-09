package com.fonsi13.blogbackend.exceptions;

import com.fonsi13.blogbackend.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("EH-01: Should return 404 for ResourceNotFoundException")
    void shouldReturn404ForResourceNotFound() {
        // Arrange
        ResourceNotFoundException ex = new ResourceNotFoundException("Post", "slug", "test-slug");

        // Act
        ResponseEntity<ApiResponse<Object>> response = handler.handleResourceNotFoundException(ex);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).contains("Post");
        assertThat(response.getBody().getMessage()).contains("slug");
    }

    @Test
    @DisplayName("EH-02: Should return 401 for CredentialsIncorrectException")
    void shouldReturn401ForCredentialsIncorrect() {
        // Arrange
        CredentialsIncorrectException ex = new CredentialsIncorrectException();

        // Act
        ResponseEntity<ApiResponse<Object>> response = handler.handleCredentialsIncorrectException(ex);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("Credenciales incorrectas");
    }

    @Test
    @DisplayName("EH-03: Should return 403 for UnauthorizedAccessException")
    void shouldReturn403ForUnauthorizedAccess() {
        // Arrange
        UnauthorizedAccessException ex = new UnauthorizedAccessException("post", "modificar");

        // Act
        ResponseEntity<ApiResponse<Object>> response = handler.handleUnauthorizedAccessException(ex);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).contains("No tienes permisos");
    }

    @Test
    @DisplayName("EH-05: Should return 500 for generic Exception and not expose internal details")
    void shouldReturn500ForGenericException() {
        // Arrange
        Exception ex = new RuntimeException("Internal MongoDB connection error with credentials");

        // Act
        ResponseEntity<ApiResponse<Object>> response = handler.handleGlobalException(ex);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        // Verify internal details are NOT exposed (OWASP A01:2021)
        assertThat(response.getBody().getMessage()).doesNotContain("MongoDB");
        assertThat(response.getBody().getMessage()).doesNotContain("credentials");
        assertThat(response.getBody().getMessage()).isEqualTo("Error interno del servidor. Intente nuevamente m\u00e1s tarde.");
    }
}
