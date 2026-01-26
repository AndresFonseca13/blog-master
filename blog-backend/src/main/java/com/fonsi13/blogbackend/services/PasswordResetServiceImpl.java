package com.fonsi13.blogbackend.services;

import com.fonsi13.blogbackend.dto.ApiResponse;
import com.fonsi13.blogbackend.models.PasswordResetToken;
import com.fonsi13.blogbackend.models.User;
import com.fonsi13.blogbackend.repositories.PasswordResetTokenRepository;
import com.fonsi13.blogbackend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    private static final int TOKEN_EXPIRATION_HOURS = 1;

    @Override
    public ApiResponse<Void> forgotPassword(String email) {
        // Siempre retornamos el mismo mensaje para no revelar si el email existe
        String successMessage = "Si el email está registrado, recibirás un enlace de recuperación";

        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            log.warn("Intento de recuperación de contraseña para email no registrado: {}", email);
            return ApiResponse.success(successMessage, null);
        }

        User user = userOpt.get();

        // Eliminar tokens anteriores del usuario
        tokenRepository.deleteByUserId(user.getId());

        // Crear nuevo token
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .userId(user.getId())
                .expiryDate(LocalDateTime.now().plusHours(TOKEN_EXPIRATION_HOURS))
                .used(false)
                .build();

        tokenRepository.save(resetToken);

        // Construir link de recuperación
        String resetLink = frontendUrl + "/reset-password?token=" + token;

        // Enviar email
        try {
            emailService.sendPasswordResetEmail(email, resetLink);
            log.info("Token de recuperación generado para usuario: {}", user.getUsername());
        } catch (Exception e) {
            log.error("Error al enviar email de recuperación: {}", e.getMessage());
            // No revelamos el error al usuario
        }

        return ApiResponse.success(successMessage, null);
    }

    @Override
    public ApiResponse<Void> resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);

        if (tokenOpt.isEmpty()) {
            log.warn("Intento de reset con token inexistente: {}", token);
            return ApiResponse.error("El enlace de recuperación no es válido");
        }

        PasswordResetToken resetToken = tokenOpt.get();

        if (!resetToken.isValid()) {
            if (resetToken.isUsed()) {
                log.warn("Intento de reutilizar token ya usado: {}", token);
                return ApiResponse.error("Este enlace ya fue utilizado");
            }
            if (resetToken.isExpired()) {
                log.warn("Intento de usar token expirado: {}", token);
                return ApiResponse.error("El enlace de recuperación ha expirado");
            }
        }

        // Buscar usuario
        Optional<User> userOpt = userRepository.findById(resetToken.getUserId());
        if (userOpt.isEmpty()) {
            log.error("Token válido pero usuario no encontrado: {}", resetToken.getUserId());
            return ApiResponse.error("Error al procesar la solicitud");
        }

        User user = userOpt.get();

        // Actualizar contraseña
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Marcar token como usado
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        log.info("Contraseña actualizada exitosamente para usuario: {}", user.getUsername());

        return ApiResponse.success("Contraseña actualizada correctamente", null);
    }

    @Override
    public ApiResponse<Boolean> validateToken(String token) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);

        if (tokenOpt.isEmpty()) {
            return ApiResponse.success("Token no encontrado", false);
        }

        PasswordResetToken resetToken = tokenOpt.get();

        if (!resetToken.isValid()) {
            String reason = resetToken.isUsed() ? "Token ya utilizado" : "Token expirado";
            return ApiResponse.success(reason, false);
        }

        return ApiResponse.success("Token válido", true);
    }
}
