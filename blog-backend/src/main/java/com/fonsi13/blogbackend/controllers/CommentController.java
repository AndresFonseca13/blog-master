package com.fonsi13.blogbackend.controllers;

import com.fonsi13.blogbackend.dto.ApiResponse;
import com.fonsi13.blogbackend.dto.CommentCreateRequest;
import com.fonsi13.blogbackend.dto.CommentResponseDTO;
import com.fonsi13.blogbackend.dto.CommentUpdateRequest;
import com.fonsi13.blogbackend.services.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/posts/{postId}/comments")
@RequiredArgsConstructor
@Tag(name = "Comments", description = "Endpoints para gestión de comentarios")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    @Operation(summary = "Crear comentario", description = "Crea un nuevo comentario en un post. Requiere autenticación.")
    public ResponseEntity<ApiResponse<CommentResponseDTO>> createComment(
            @PathVariable String postId,
            @Valid @RequestBody CommentCreateRequest request,
            Authentication authentication
    ){
        return ResponseEntity.ok(
                commentService.createComment(postId, request, authentication.getName())
        );
    }

    @GetMapping
    @Operation(summary = "Obtener comentarios", description = "Retorna una lista paginada de comentarios de un post.")
    public ResponseEntity<ApiResponse<Page<CommentResponseDTO>>> getComments(
            @PathVariable String postId,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ){
        return ResponseEntity.ok(
                commentService.getCommentByPost(postId, pageable)
        );
    }

    @PutMapping("/{commentId}")
    @Operation(summary = "Actualizar comentario", description = "Actualiza un comentario existente. Solo el autor o un ADMIN puede actualizar.")
    public ResponseEntity<ApiResponse<CommentResponseDTO>> updateComment(
            @PathVariable String postId,
            @PathVariable String commentId,
            @Valid @RequestBody CommentUpdateRequest request,
            Authentication authentication
    ){
        return ResponseEntity.ok(
                commentService.updateComment(postId, commentId, request, authentication.getName())
        );
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "Eliminar comentario", description = "Elimina un comentario existente. Solo el autor o un ADMIN puede eliminar.")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable String postId,
            @PathVariable String commentId,
            Authentication authentication
    ){
        return ResponseEntity.ok(
                commentService.deleteComment(postId, commentId, authentication.getName())
        );
    }
}
