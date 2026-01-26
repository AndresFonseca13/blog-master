package com.fonsi13.blogbackend.controllers;

import com.fonsi13.blogbackend.dto.ApiResponse;
import com.fonsi13.blogbackend.dto.ForgotPasswordRequest;
import com.fonsi13.blogbackend.dto.ResetPasswordRequest;
import com.fonsi13.blogbackend.services.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints de autenticación y recuperación de contraseña")
public class AuthController {

    private final PasswordResetService passwordResetService;

    @Operation(
            summary = "Solicitar recuperación de contraseña",
            description = "Envía un email con un enlace para restablecer la contraseña. Por seguridad, siempre retorna éxito aunque el email no exista."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Solicitud procesada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Email inválido")
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        ApiResponse<Void> response = passwordResetService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Restablecer contraseña",
            description = "Restablece la contraseña usando el token enviado por email. El token expira en 1 hora y solo puede usarse una vez."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Contraseña actualizada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Token inválido o expirado")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        ApiResponse<Void> response = passwordResetService.resetPassword(request.getToken(), request.getNewPassword());

        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Validar token de recuperación",
            description = "Verifica si un token de recuperación es válido antes de mostrar el formulario de nueva contraseña"
    )
    @GetMapping("/validate-reset-token")
    public ResponseEntity<ApiResponse<Boolean>> validateResetToken(@RequestParam String token) {
        ApiResponse<Boolean> response = passwordResetService.validateToken(token);
        return ResponseEntity.ok(response);
    }
}
