package com.fonsi13.blogbackend.services;

import com.fonsi13.blogbackend.config.security.JwtService;
import com.fonsi13.blogbackend.dto.*;
import com.fonsi13.blogbackend.exceptions.CredentialsIncorrectException;
import com.fonsi13.blogbackend.exceptions.ResourceNotFoundException;
import com.fonsi13.blogbackend.models.AuthProvider;
import com.fonsi13.blogbackend.models.User;
import com.fonsi13.blogbackend.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserRegistrationRequest registrationRequest;
    private UserLoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id("user-123")
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .role("USER")
                .provider(AuthProvider.LOCAL)
                .emailVerified(false)
                .createdAt(LocalDateTime.now())
                .build();

        registrationRequest = new UserRegistrationRequest();
        registrationRequest.setUsername("testuser");
        registrationRequest.setEmail("test@example.com");
        registrationRequest.setPassword("password123");

        loginRequest = new UserLoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");
    }

    @Nested
    @DisplayName("US-01 to US-03: Register User")
    class RegisterUserTests {

        @Test
        @DisplayName("US-01: Should register user successfully with valid data")
        void shouldRegisterUserSuccessfully() {
            // Arrange
            when(userRepository.existsByUsername("testuser")).thenReturn(false);
            when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // Act
            ApiResponse<UserResponseDTO> response = userService.registerUser(registrationRequest);

            // Assert
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getMessage()).isEqualTo("Registro exitoso");
            assertThat(response.getData()).isNotNull();
            assertThat(response.getData().getUsername()).isEqualTo("testuser");
            assertThat(response.getData().getEmail()).isEqualTo("test@example.com");
            assertThat(response.getData().getRole()).isEqualTo("USER");

            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("US-02: Should return error when username already exists")
        void shouldReturnErrorWhenUsernameExists() {
            // Arrange
            when(userRepository.existsByUsername("testuser")).thenReturn(true);

            // Act
            ApiResponse<UserResponseDTO> response = userService.registerUser(registrationRequest);

            // Assert
            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getMessage()).contains("ya est\u00e1 en uso");

            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("US-03: Should return error when email already exists")
        void shouldReturnErrorWhenEmailExists() {
            // Arrange
            when(userRepository.existsByUsername("testuser")).thenReturn(false);
            when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

            // Act
            ApiResponse<UserResponseDTO> response = userService.registerUser(registrationRequest);

            // Assert
            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getMessage()).contains("ya se encuentra registrado");

            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("US-04 to US-07: Login")
    class LoginTests {

        @Test
        @DisplayName("US-04: Should login successfully with correct credentials")
        void shouldLoginSuccessfully() {
            // Arrange
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
            when(jwtService.generateToken("testuser")).thenReturn("jwt-token-123");

            // Act
            ApiResponse<AuthResponseDTO> response = userService.login(loginRequest);

            // Assert
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getMessage()).isEqualTo("Login exitoso");
            assertThat(response.getData()).isNotNull();
            assertThat(response.getData().getToken()).isEqualTo("jwt-token-123");
            assertThat(response.getData().getUser().getUsername()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("US-05: Should throw CredentialsIncorrectException when username not found")
        void shouldThrowWhenUsernameNotFound() {
            // Arrange
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> userService.login(loginRequest))
                    .isInstanceOf(CredentialsIncorrectException.class);
        }

        @Test
        @DisplayName("US-06: Should return error when password is incorrect")
        void shouldReturnErrorWhenPasswordIncorrect() {
            // Arrange
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(false);

            // Act
            ApiResponse<AuthResponseDTO> response = userService.login(loginRequest);

            // Assert
            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getMessage()).contains("Credenciales incorrectas");
        }

        @Test
        @DisplayName("US-07: Should return error when OAuth2 user tries local login without password")
        void shouldReturnErrorWhenOAuth2UserWithoutPassword() {
            // Arrange
            User oauthUser = User.builder()
                    .id("oauth-user-1")
                    .username("googleuser")
                    .email("google@example.com")
                    .password(null)
                    .role("USER")
                    .provider(AuthProvider.GOOGLE)
                    .build();

            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(oauthUser));

            // Act
            ApiResponse<AuthResponseDTO> response = userService.login(loginRequest);

            // Assert
            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getMessage()).contains("solo puede iniciar sesi\u00f3n con");
            assertThat(response.getMessage()).contains("GOOGLE");
        }
    }

    @Nested
    @DisplayName("US-08: Get Current User")
    class GetCurrentUserTests {

        @Test
        @DisplayName("US-08: Should return current user DTO for valid username")
        void shouldReturnCurrentUser() {
            // Arrange
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

            // Act
            UserResponseDTO result = userService.getCurrentUser("testuser");

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("user-123");
            assertThat(result.getUsername()).isEqualTo("testuser");
            assertThat(result.getEmail()).isEqualTo("test@example.com");
            assertThat(result.getProvider()).isEqualTo(AuthProvider.LOCAL);
        }

        @Test
        @DisplayName("US-08b: Should throw ResourceNotFoundException when user not found")
        void shouldThrowWhenUserNotFound() {
            // Arrange
            when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> userService.getCurrentUser("nonexistent"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
