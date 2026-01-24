package com.fonsi13.blogbackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class PostCreateRequest {
    @NotBlank(message = "El título es obligatorio")
    private String title;

    @NotBlank(message = "El contenido es obligatorio")
    private String content;

    private String summary;

    private String coverImage; // La portada principal

    private List<String> images; // Lista de URLs de imágenes extra

    private List<String> videoUrls; // Lista de links de YouTube/Vimeo

    private List<String> topics;
}
