package com.fonsi13.blogbackend.dto;

import com.fonsi13.blogbackend.models.AuthProvider;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponseDTO {
    private String id;
    private String username;
    private String email;
    private String role;
    private LocalDateTime createdAt;
    private AuthProvider provider;
    private String profilePicture;
    private boolean emailVerified;
}
