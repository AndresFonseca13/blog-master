package com.fonsi13.blogbackend.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "posts")
public class Post {

    @Id
    private String id;

    private String title;
    private String slug;
    private String content;
    private String summary;

    // Renombramos mediaUrl a coverImage para ser explícitos
    private String coverImage;

    // Lista para una galería de imágenes extra
    private List<String> images;

    // Lista de links de video (YouTube/Vimeo)
    private List<String> videoUrls;

    private String authorId;

    private List<String> topics;

    private PostStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
