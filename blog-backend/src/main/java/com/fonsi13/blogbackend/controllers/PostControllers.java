package com.fonsi13.blogbackend.controllers;

import com.fonsi13.blogbackend.dto.ApiResponse;
import com.fonsi13.blogbackend.dto.PostCreateRequest;
import com.fonsi13.blogbackend.dto.PostResponseDTO;
import com.fonsi13.blogbackend.services.PostService;
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
public class PostControllers {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<ApiResponse<PostResponseDTO>> createPost(
            @RequestBody PostCreateRequest request,
            Authentication authentication
    ){
        String currentUsername = authentication.getName();

        ApiResponse<PostResponseDTO> response = postService.createPost(request, currentUsername);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PostResponseDTO>>> getAllPosts(
            @PageableDefault(size = 10, sort = "createdAt")Pageable pageable
    ){
        ApiResponse<Page<PostResponseDTO>> response = postService.getAllPosts(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<PostResponseDTO>> getPostBySlug(@PathVariable String slug){
        ApiResponse<PostResponseDTO> response = postService.getPostBySlug(slug);
        return ResponseEntity.ok(response);
    }

}
