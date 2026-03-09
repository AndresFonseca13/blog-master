package com.fonsi13.blogbackend.services;

import com.fonsi13.blogbackend.dto.ApiResponse;
import com.fonsi13.blogbackend.models.PasswordResetToken;
import com.fonsi13.blogbackend.models.User;
import com.fonsi13.blogbackend.repositories.PasswordResetTokenRepository;
import com.fonsi13.blogbackend.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PasswordResetServiceImpl Tests")
class PasswordResetServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordResetServiceImpl passwordResetService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id("user-123")
                .username("testuser")
                .email("test@example.com")
                .password("oldEncodedPassword")
                .role("USER")
                .build();
    }

    @Nested
    @DisplayName("PR-01 to PR-03: Forgot Password")
    class ForgotPasswordTests {

        @Test
        @DisplayName("PR-01: Should generate token and send email for registered user")
        void shouldGenerateTokenAndSendEmail() {
            // Arrange
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
            when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            ApiResponse<Void> response = passwordResetService.forgotPassword("test@example.com");

            // Assert
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getMessage()).contains("Si el email est\u00e1 registrado");

            // Verify token was saved
            ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
            verify(tokenRepository).save(tokenCaptor.capture());

            PasswordResetToken savedToken = tokenCaptor.getValue();
            assertThat(savedToken.getUserId()).isEqualTo("user-123");
            assertThat(savedToken.getToken()).isNotNull();
            assertThat(savedToken.getExpiryDate()).isAfter(LocalDateTime.now());
            assertThat(savedToken.isUsed()).isFalse();

            // Verify email was sent
            verify(emailService).sendPasswordResetEmail(eq("test@example.com"), anyString());
        }

        @Test
        @DisplayName("PR-02: Should return same message for unregistered email (security)")
        void shouldReturnSameMessageForUnregisteredEmail() {
            // Arrange
            when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

            // Act
            ApiResponse<Void> response = passwordResetService.forgotPassword("unknown@example.com");

            // Assert
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getMessage()).contains("Si el email est\u00e1 registrado");

            // Verify NO token was saved and NO email was sent
            verify(tokenRepository, never()).save(any());
            verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
        }

        @Test
        @DisplayName("PR-03: Should delete previous tokens before creating new one")
        void shouldDeletePreviousTokens() {
            // Arrange
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
            when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            passwordResetService.forgotPassword("test@example.com");

            // Assert - verify old tokens deleted before new one saved
            verify(tokenRepository).deleteByUserId("user-123");
            verify(tokenRepository).save(any(PasswordResetToken.class));
        }
    }

    @Nested
    @DisplayName("PR-04 to PR-07: Reset Password")
    class ResetPasswordTests {

        @Test
        @DisplayName("PR-04: Should reset password with valid token")
        void shouldResetPasswordWithValidToken() {
            // Arrange
            PasswordResetToken validToken = PasswordResetToken.builder()
                    .id("token-id")
                    .token("valid-token-uuid")
                    .userId("user-123")
                    .expiryDate(LocalDateTime.now().plusHours(1))
                    .used(false)
                    .build();

            when(tokenRepository.findByToken("valid-token-uuid")).thenReturn(Optional.of(validToken));
            when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));
            when(passwordEncoder.encode("newPassword123")).thenReturn("newEncodedPassword");

            // Act
            ApiResponse<Void> response = passwordResetService.resetPassword("valid-token-uuid", "newPassword123");

            // Assert
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getMessage()).contains("actualizada correctamente");

            // Verify password was updated
            verify(userRepository).save(argThat(user -> user.getPassword().equals("newEncodedPassword")));

            // Verify token was marked as used
            verify(tokenRepository).save(argThat(PasswordResetToken::isUsed));
        }

        @Test
        @DisplayName("PR-05: Should return error for non-existent token")
        void shouldReturnErrorForNonExistentToken() {
            // Arrange
            when(tokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

            // Act
            ApiResponse<Void> response = passwordResetService.resetPassword("invalid-token", "newPassword123");

            // Assert
            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getMessage()).contains("no es v\u00e1lido");
        }

        @Test
        @DisplayName("PR-06: Should return error for expired token")
        void shouldReturnErrorForExpiredToken() {
            // Arrange
            PasswordResetToken expiredToken = PasswordResetToken.builder()
                    .id("token-id")
                    .token("expired-token")
                    .userId("user-123")
                    .expiryDate(LocalDateTime.now().minusHours(2))
                    .used(false)
                    .build();

            when(tokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expiredToken));

            // Act
            ApiResponse<Void> response = passwordResetService.resetPassword("expired-token", "newPassword123");

            // Assert
            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getMessage()).contains("expirado");
        }

        @Test
        @DisplayName("PR-07: Should return error for already used token")
        void shouldReturnErrorForUsedToken() {
            // Arrange
            PasswordResetToken usedToken = PasswordResetToken.builder()
                    .id("token-id")
                    .token("used-token")
                    .userId("user-123")
                    .expiryDate(LocalDateTime.now().plusHours(1))
                    .used(true)
                    .build();

            when(tokenRepository.findByToken("used-token")).thenReturn(Optional.of(usedToken));

            // Act
            ApiResponse<Void> response = passwordResetService.resetPassword("used-token", "newPassword123");

            // Assert
            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getMessage()).contains("ya fue utilizado");
        }
    }

    @Nested
    @DisplayName("PR-08: Validate Token")
    class ValidateTokenTests {

        @Test
        @DisplayName("PR-08a: Should return true for valid token")
        void shouldReturnTrueForValidToken() {
            // Arrange
            PasswordResetToken validToken = PasswordResetToken.builder()
                    .token("valid-token")
                    .userId("user-123")
                    .expiryDate(LocalDateTime.now().plusHours(1))
                    .used(false)
                    .build();

            when(tokenRepository.findByToken("valid-token")).thenReturn(Optional.of(validToken));

            // Act
            ApiResponse<Boolean> response = passwordResetService.validateToken("valid-token");

            // Assert
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getData()).isTrue();
        }

        @Test
        @DisplayName("PR-08b: Should return false for non-existent token")
        void shouldReturnFalseForNonExistentToken() {
            // Arrange
            when(tokenRepository.findByToken("missing")).thenReturn(Optional.empty());

            // Act
            ApiResponse<Boolean> response = passwordResetService.validateToken("missing");

            // Assert
            assertThat(response.getData()).isFalse();
        }
    }
}
