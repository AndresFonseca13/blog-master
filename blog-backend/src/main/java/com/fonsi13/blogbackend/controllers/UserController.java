package com.fonsi13.blogbackend.controllers;

import com.fonsi13.blogbackend.dto.*;
import com.fonsi13.blogbackend.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("register")
    public ResponseEntity<ApiResponse<UserResponseDTO>> register(@RequestBody UserRegistrationRequest request){
        ApiResponse<UserResponseDTO> response = userService.registerUser(request);

        if (response.isSuccess()){
            return ResponseEntity.ok(response);
        }else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> login(@RequestBody UserLoginRequest request) {
        ApiResponse<AuthResponseDTO> response = userService.login(request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            // Un 401 Unauthorized es el código HTTP correcto para fallos de login
            return ResponseEntity.status(401).body(response);
        }
    }

    @GetMapping("/me") // GET /api/v1/users/me
    public ResponseEntity<ApiResponse<String>> sayHello() {
        return ResponseEntity.ok(ApiResponse.success("¡Bienvenido! Has entrado a una zona segura.", "Datos secretos aquí"));
    }

}
