package com.fonsi13.blogbackend.services;

import com.fonsi13.blogbackend.dto.ApiResponse;

public interface PasswordResetService {

    ApiResponse<Void> forgotPassword(String email);

    ApiResponse<Void> resetPassword(String token, String newPassword);

    ApiResponse<Boolean> validateToken(String token);
}
