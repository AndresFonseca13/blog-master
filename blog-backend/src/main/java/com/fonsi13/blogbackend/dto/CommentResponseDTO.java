package com.fonsi13.blogbackend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentResponseDTO {

    private String id;
    private String content;
    private String authorId;
    private LocalDateTime createdAt;
}
