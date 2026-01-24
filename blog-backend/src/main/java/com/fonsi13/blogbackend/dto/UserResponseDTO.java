package com.fonsi13.blogbackend.dto;

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


}
