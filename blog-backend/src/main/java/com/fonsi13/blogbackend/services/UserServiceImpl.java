package com.fonsi13.blogbackend.services;

import com.fonsi13.blogbackend.config.security.JwtService;
import com.fonsi13.blogbackend.dto.*;
import com.fonsi13.blogbackend.exceptions.ResourceNotFoundException;
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
        // Mapeo manual de DTO a entidad
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); //TODO: ByCript
        user.setRole("USER");
        user.setCreatedAt(LocalDateTime.now());

        //Guardar en mongo
        User savedUser = userRepository.save(user);

        //Mapeo de entidad a ResponseDTO
        UserResponseDTO responseData = UserResponseDTO.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .createdAt(savedUser.getCreatedAt())
                .build();

        return ApiResponse.success("Registo exitoso", responseData);
    }

    @Override
    public ApiResponse<AuthResponseDTO> login(UserLoginRequest request) {
        //Buscar usuario por email
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "username", request.getUsername()));

        //Verificar la contraseña
        boolean isPasswordMatch = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!isPasswordMatch){
            return ApiResponse.error("Credenciales incorrectas");
        }

        String token = jwtService.generateToken(user.getUsername());

        //Preparar los datos del usuario
        UserResponseDTO userData = UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();

        //Empaquetar t odo en la nueva respuesta
        AuthResponseDTO authResponse = AuthResponseDTO.builder()
                .token(token)
                .user(userData)
                .build();


        return ApiResponse.success("Login exitoso", authResponse);
    }
}
