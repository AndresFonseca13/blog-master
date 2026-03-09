package com.fonsi13.blogbackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegistrationRequest {

    @NotBlank(message = "El nombre de usuario es obligatorio")
    private String username;

    @Email(message = "El formato de email no es valido")
    private String email;

    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;

}
