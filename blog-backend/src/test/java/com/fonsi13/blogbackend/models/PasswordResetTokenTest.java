package com.fonsi13.blogbackend.models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PasswordResetToken Model Tests")
class PasswordResetTokenTest {

    @Test
    @DisplayName("PT-01: isExpired should return false for future expiry date")
    void shouldNotBeExpiredForFutureDate() {
        // Arrange
        PasswordResetToken token = PasswordResetToken.builder()
                .token("test-token")
                .userId("user-123")
                .expiryDate(LocalDateTime.now().plusHours(1))
                .used(false)
                .build();

        // Act & Assert
        assertThat(token.isExpired()).isFalse();
    }

    @Test
    @DisplayName("PT-02: isExpired should return true for past expiry date")
    void shouldBeExpiredForPastDate() {
        // Arrange
        PasswordResetToken token = PasswordResetToken.builder()
                .token("test-token")
                .userId("user-123")
                .expiryDate(LocalDateTime.now().minusHours(1))
                .used(false)
                .build();

        // Act & Assert
        assertThat(token.isExpired()).isTrue();
    }

    @Test
    @DisplayName("PT-03: isValid should return true for unused and non-expired token")
    void shouldBeValidForUnusedAndNonExpiredToken() {
        // Arrange
        PasswordResetToken token = PasswordResetToken.builder()
                .token("test-token")
                .userId("user-123")
                .expiryDate(LocalDateTime.now().plusHours(1))
                .used(false)
                .build();

        // Act & Assert
        assertThat(token.isValid()).isTrue();
    }

    @Test
    @DisplayName("PT-03b: isValid should return false for used token")
    void shouldBeInvalidForUsedToken() {
        // Arrange
        PasswordResetToken token = PasswordResetToken.builder()
                .token("test-token")
                .userId("user-123")
                .expiryDate(LocalDateTime.now().plusHours(1))
                .used(true)
                .build();

        // Act & Assert
        assertThat(token.isValid()).isFalse();
    }

    @Test
    @DisplayName("PT-03c: isValid should return false for expired token")
    void shouldBeInvalidForExpiredToken() {
        // Arrange
        PasswordResetToken token = PasswordResetToken.builder()
                .token("test-token")
                .userId("user-123")
                .expiryDate(LocalDateTime.now().minusMinutes(1))
                .used(false)
                .build();

        // Act & Assert
        assertThat(token.isValid()).isFalse();
    }
}
