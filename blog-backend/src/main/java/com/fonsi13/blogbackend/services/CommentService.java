package com.fonsi13.blogbackend.services;

import com.fonsi13.blogbackend.dto.ApiResponse;
import com.fonsi13.blogbackend.dto.CommentCreateRequest;
import com.fonsi13.blogbackend.dto.CommentResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentService {
    ApiResponse<CommentResponseDTO> createComment(String postId, CommentCreateRequest request, String currentUsername);
    ApiResponse<Page<CommentResponseDTO>> getCommentByPost(String postId, Pageable pageable);
}
