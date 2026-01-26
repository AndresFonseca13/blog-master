package com.fonsi13.blogbackend.controllers;

import com.fonsi13.blogbackend.dto.ApiResponse;
import com.fonsi13.blogbackend.dto.PostCreateRequest;
import com.fonsi13.blogbackend.dto.PostResponseDTO;
import com.fonsi13.blogbackend.dto.PostUpdateRequest;
import com.fonsi13.blogbackend.services.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
@Tag(name = "Posts", description = "Endpoints para gestión de posts del blog")
public class PostControllers {

    private final PostService postService;

    @PostMapping
    @Operation(summary = "Crear un nuevo post", description = "Crea un nuevo post. Requiere autenticación.")
    public ResponseEntity<ApiResponse<PostResponseDTO>> createPost(
            @RequestBody PostCreateRequest request,
            Authentication authentication
    ){
        String currentUsername = authentication.getName();

        ApiResponse<PostResponseDTO> response = postService.createPost(request, currentUsername);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Obtener todos los posts", description = "Retorna una lista paginada de posts.")
    public ResponseEntity<ApiResponse<Page<PostResponseDTO>>> getAllPosts(
            @PageableDefault(size = 10, sort = "createdAt")Pageable pageable
    ){
        ApiResponse<Page<PostResponseDTO>> response = postService.getAllPosts(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Obtener post por slug", description = "Retorna un post específico por su slug.")
    public ResponseEntity<ApiResponse<PostResponseDTO>> getPostBySlug(@PathVariable String slug){
        ApiResponse<PostResponseDTO> response = postService.getPostBySlug(slug);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un post", description = "Actualiza un post existente. Solo el autor o un ADMIN puede actualizar.")
    public ResponseEntity<ApiResponse<PostResponseDTO>> updatePost(
            @PathVariable String id,
            @RequestBody PostUpdateRequest request,
            Authentication authentication
    ){
        String currentUsername = authentication.getName();
        ApiResponse<PostResponseDTO> response = postService.updatePost(id, request, currentUsername);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un post", description = "Elimina un post existente. Solo el autor o un ADMIN puede eliminar.")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable String id,
            Authentication authentication
    ){
        String currentUsername = authentication.getName();
        ApiResponse<Void> response = postService.deletePost(id, currentUsername);
        return ResponseEntity.ok(response);
    }

}
