package com.fonsi13.blogbackend.services;

import com.fonsi13.blogbackend.dto.ApiResponse;
import com.fonsi13.blogbackend.dto.CommentCreateRequest;
import com.fonsi13.blogbackend.dto.CommentResponseDTO;
import com.fonsi13.blogbackend.exceptions.ResourceNotFoundException;
import com.fonsi13.blogbackend.models.Comment;
import com.fonsi13.blogbackend.models.User;
import com.fonsi13.blogbackend.repositories.CommentRepository;
import com.fonsi13.blogbackend.repositories.PostRepository;
import com.fonsi13.blogbackend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService{

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;


    @Override
    public ApiResponse<CommentResponseDTO> createComment(String postId, CommentCreateRequest request, String currentUsername) {
        // Validar que el post existe
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Post", "id", postId);
        }


        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "username", currentUsername));

        Comment comment = Comment.builder()
                .content(request.getContent())
                .postId(postId)
                .authorId(user.getId())
                .createdAt(LocalDateTime.now())
                .build();

        Comment saved = commentRepository.save(comment);

        // Convertir a DTO
        CommentResponseDTO response = CommentResponseDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .authorId(comment.getAuthorId())
                .createdAt(comment.getCreatedAt())
                .build();

        return ApiResponse.success("Comentario agregado", response);
    }

    @Override
    public ApiResponse<Page<CommentResponseDTO>> getCommentByPost(String postId, Pageable pageable) {
        // Traer paginado desde el repo
        Page<Comment> comments = commentRepository.findByPostId(postId, pageable);

        //Convetir a DTO
        Page<CommentResponseDTO> dtoPage = comments.map(c -> CommentResponseDTO.builder()
                .id(c.getId())
                .content(c.getContent())
                .authorId(c.getAuthorId())
                .createdAt(c.getCreatedAt())
                .build());

        return ApiResponse.success("Comentatios del post", dtoPage);
    }
}
