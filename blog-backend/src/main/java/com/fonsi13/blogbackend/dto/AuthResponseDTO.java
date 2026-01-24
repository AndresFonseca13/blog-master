package com.fonsi13.blogbackend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponseDTO {
    private String token;
    private UserResponseDTO user;
}
