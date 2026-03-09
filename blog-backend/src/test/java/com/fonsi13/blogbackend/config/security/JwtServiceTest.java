package com.fonsi13.blogbackend.config.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JwtService Tests")
class JwtServiceTest {

    private JwtService jwtService;

    // A valid 256-bit (32+ chars) secret key for HS256
    private static final String VALID_SECRET = "this-is-a-very-secure-secret-key-for-testing-purposes-2026";
    private static final long EXPIRATION = 86400000L; // 24 hours

    @BeforeEach
    void setUp() throws Exception {
        jwtService = new JwtService();
        setField(jwtService, "secretKey", VALID_SECRET);
        setField(jwtService, "jwtExpiration", EXPIRATION);
    }

    /**
     * Helper to set private fields via reflection (since we are testing without Spring context).
     */
    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Nested
    @DisplayName("JW-01 to JW-02: Token Generation and Validation")
    class TokenGenerationTests {

        @Test
        @DisplayName("JW-01: Should generate token and extract correct username")
        void shouldGenerateTokenAndExtractUsername() {
            // Act
            String token = jwtService.generateToken("testuser");

            // Assert
            assertThat(token).isNotNull().isNotEmpty();
            String extractedUsername = jwtService.extractUsername(token);
            assertThat(extractedUsername).isEqualTo("testuser");
        }

        @Test
        @DisplayName("JW-02: Should validate correct token as valid")
        void shouldValidateCorrectToken() {
            // Arrange
            String token = jwtService.generateToken("testuser");

            // Act
            boolean isValid = jwtService.isTokenValid(token, "testuser");

            // Assert
            assertThat(isValid).isTrue();
        }
    }

    @Nested
    @DisplayName("JW-03 to JW-04: Token Rejection")
    class TokenRejectionTests {

        @Test
        @DisplayName("JW-03: Should reject token when username does not match")
        void shouldRejectTokenWithWrongUsername() {
            // Arrange
            String token = jwtService.generateToken("testuser");

            // Act
            boolean isValid = jwtService.isTokenValid(token, "differentuser");

            // Assert
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("JW-04: Should reject expired token")
        void shouldRejectExpiredToken() throws Exception {
            // Arrange - create a service with 0ms expiration (token expires immediately)
            JwtService shortLivedService = new JwtService();
            setField(shortLivedService, "secretKey", VALID_SECRET);
            setField(shortLivedService, "jwtExpiration", 0L);

            String token = shortLivedService.generateToken("testuser");

            // Small delay to ensure token expires
            Thread.sleep(10);

            // Act & Assert
            assertThatThrownBy(() -> shortLivedService.isTokenValid(token, "testuser"))
                    .isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("JW-05: Secret Key Validation")
    class SecretKeyTests {

        @Test
        @DisplayName("JW-05: Should throw IllegalArgumentException for short secret key")
        void shouldThrowForShortSecretKey() throws Exception {
            // Arrange
            JwtService weakService = new JwtService();
            setField(weakService, "secretKey", "short");
            setField(weakService, "jwtExpiration", EXPIRATION);

            // Act & Assert
            assertThatThrownBy(() -> weakService.generateToken("testuser"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("32 caracteres");
        }
    }
}
