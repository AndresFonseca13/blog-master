package com.fonsi13.blogbackend.services;

import com.fonsi13.blogbackend.config.security.JwtService;
import com.fonsi13.blogbackend.dto.*;
import com.fonsi13.blogbackend.exceptions.ResourceNotFoundException;
import com.fonsi13.blogbackend.models.AuthProvider;
import com.fonsi13.blogbackend.models.User;
import com.fonsi13.blogbackend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;


    @Override
    public ApiResponse<UserResponseDTO> registerUser(UserRegistrationRequest request) {
        // Verificar si el nombre de usuario existe
        if (userRepository.existsByUsername(request.getUsername())){
            return ApiResponse.error("El nombre de usuario '" + request.getUsername() + "' ya está en uso.");
        }

        if (userRepository.existsByEmail(request.getEmail())){
            return ApiResponse.error("El correo ya se encuentra registrado.");
        }

        // Crear usuario con builder
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("USER")
                .provider(AuthProvider.LOCAL)
                .emailVerified(false)
                .createdAt(LocalDateTime.now())
                .build();

        // Guardar en mongo
        User savedUser = userRepository.save(user);

        return ApiResponse.success("Registro exitoso", mapToDTO(savedUser));
    }

    @Override
    public ApiResponse<AuthResponseDTO> login(UserLoginRequest request) {
        // Buscar usuario por username
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "username", request.getUsername()));

        // Verificar que el usuario tenga password (no sea solo OAuth)
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            return ApiResponse.error("Este usuario solo puede iniciar sesión con " + user.getProvider());
        }

        // Verificar la contraseña
        boolean isPasswordMatch = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!isPasswordMatch){
            return ApiResponse.error("Credenciales incorrectas");
        }

        String token = jwtService.generateToken(user.getUsername());

        // Empaquetar respuesta
        AuthResponseDTO authResponse = AuthResponseDTO.builder()
                .token(token)
                .user(mapToDTO(user))
                .build();

        return ApiResponse.success("Login exitoso", authResponse);
    }

    // Método auxiliar para mapear User a UserResponseDTO
    private UserResponseDTO mapToDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .provider(user.getProvider())
                .profilePicture(user.getProfilePicture())
                .emailVerified(user.isEmailVerified())
                .build();
    }
}
