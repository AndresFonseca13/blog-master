package com.fonsi13.blogbackend.dto;

import com.fonsi13.blogbackend.models.PostStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PostResponseDTO {
    private String id;
    private String title;
    private String slug;
    private String content;
    private String summary;
    private String coverImage; // La portada principal
    private List<String> images; // Lista de URLs de imágenes extra
    private List<String> videoUrls; // Lista de links de YouTube/Vimeo
    private String authorId; // Más adelante aprenderemos a devolver el nombre del autor aquí
    private List<String> topics;
    private PostStatus status;
    private LocalDateTime createdAt;
}
