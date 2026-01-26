package com.fonsi13.blogbackend.services;

import com.fonsi13.blogbackend.dto.ApiResponse;
import com.fonsi13.blogbackend.dto.PostCreateRequest;
import com.fonsi13.blogbackend.dto.PostResponseDTO;
import com.fonsi13.blogbackend.dto.PostUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostService {

    ApiResponse<PostResponseDTO> createPost(PostCreateRequest request, String currentUsername);

    // Devuelve una "Página" de posts, no una lista simple
    ApiResponse<Page<PostResponseDTO>> getAllPosts(Pageable pageable);

    // Leer un solo post por su URL (Slug)
    ApiResponse<PostResponseDTO> getPostBySlug(String slug);

    // Actualizar un post existente
    ApiResponse<PostResponseDTO> updatePost(String postId, PostUpdateRequest request, String currentUsername);

    // Eliminar un post
    ApiResponse<Void> deletePost(String postId, String currentUsername);
}
