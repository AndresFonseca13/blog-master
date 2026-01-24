package com.fonsi13.blogbackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentCreateRequest {
    @NotBlank(message = "El comentario no puede estar vacio")
    private String content;

}
