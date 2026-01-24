package com.fonsi13.blogbackend.controllers;

import com.fonsi13.blogbackend.dto.ApiResponse;
import com.fonsi13.blogbackend.dto.CommentCreateRequest;
import com.fonsi13.blogbackend.dto.CommentResponseDTO;
import com.fonsi13.blogbackend.services.CommentService;
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
public class CommentController {

    private final CommentService commentService;

    //Crear comentario
    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponseDTO>> createComment(
            @PathVariable String postId,
            @RequestBody CommentCreateRequest request,
            Authentication authentication
    ){
        return ResponseEntity.ok(
                commentService.createComment(postId, request, authentication.getName())
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CommentResponseDTO>>> getComments(
            @PathVariable String postId,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ){
        return ResponseEntity.ok(
                commentService.getCommentByPost(postId, pageable)
        );
    }
}
