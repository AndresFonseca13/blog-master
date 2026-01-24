package com.fonsi13.blogbackend.services;

import com.fonsi13.blogbackend.dto.ApiResponse;
import com.fonsi13.blogbackend.dto.PostCreateRequest;
import com.fonsi13.blogbackend.dto.PostResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostService {

    ApiResponse<PostResponseDTO> createPost(PostCreateRequest request, String currentUsername);

    // Nuevo método: Devuelve una "Página" de posts, no una lista simple
    ApiResponse<Page<PostResponseDTO>> getAllPosts(Pageable pageable);

    // Nuevo método: Leer un solo post por su URL (Slug)
    ApiResponse<PostResponseDTO> getPostBySlug(String slug);
}
