package com.fonsi13.blogbackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentUpdateRequest {

    @NotBlank(message = "El contenido del comentario no puede estar vacío")
    private String content;
}
