package com.fonsi13.blogbackend.controllers;

import com.fonsi13.blogbackend.dto.*;
import com.fonsi13.blogbackend.models.User;
import com.fonsi13.blogbackend.repositories.UserRepository;
import com.fonsi13.blogbackend.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponseDTO>> register(@Valid @RequestBody UserRegistrationRequest request){
        ApiResponse<UserResponseDTO> response = userService.registerUser(request);

        if (response.isSuccess()){
            return ResponseEntity.ok(response);
        }else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> login(@Valid @RequestBody UserLoginRequest request) {
        ApiResponse<AuthResponseDTO> response = userService.login(request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            // Un 401 Unauthorized es el código HTTP correcto para fallos de login
            return ResponseEntity.status(401).body(response);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        UserResponseDTO userDTO = userService.getCurrentUser(username);

        return ResponseEntity.ok(ApiResponse.success("Usuario obtenido correctamente", userDTO));
    }

}
