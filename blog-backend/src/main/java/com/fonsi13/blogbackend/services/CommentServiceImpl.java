package com.fonsi13.blogbackend.services;

import com.fonsi13.blogbackend.dto.ApiResponse;
import com.fonsi13.blogbackend.dto.CommentCreateRequest;
import com.fonsi13.blogbackend.dto.CommentResponseDTO;
import com.fonsi13.blogbackend.dto.CommentUpdateRequest;
import com.fonsi13.blogbackend.exceptions.ResourceNotFoundException;
import com.fonsi13.blogbackend.exceptions.UnauthorizedAccessException;
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

        return ApiResponse.success("Comentario agregado", mapToDTO(saved));
    }

    @Override
    public ApiResponse<Page<CommentResponseDTO>> getCommentByPost(String postId, Pageable pageable) {
        // Traer paginado desde el repo
        Page<Comment> comments = commentRepository.findByPostId(postId, pageable);

        // Convertir a DTO
        Page<CommentResponseDTO> dtoPage = comments.map(this::mapToDTO);

        return ApiResponse.success("Comentarios del post", dtoPage);
    }

    @Override
    public ApiResponse<CommentResponseDTO> updateComment(String postId, String commentId, CommentUpdateRequest request, String currentUsername) {
        // Buscar el comentario
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comentario", "id", commentId));

        // Validar que el comentario pertenece al post especificado
        if (!comment.getPostId().equals(postId)) {
            throw new ResourceNotFoundException("Comentario", "postId", postId);
        }

        // Buscar el usuario actual
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "username", currentUsername));

        // Verificar permisos: solo el autor o un ADMIN puede actualizar
        if (!comment.getAuthorId().equals(currentUser.getId()) && !"ADMIN".equals(currentUser.getRole())) {
            throw new UnauthorizedAccessException("comentario", "modificar");
        }

        // Actualizar el contenido
        comment.setContent(request.getContent());
        comment.setUpdatedAt(LocalDateTime.now());

        Comment updatedComment = commentRepository.save(comment);

        return ApiResponse.success("Comentario actualizado exitosamente", mapToDTO(updatedComment));
    }

    @Override
    public ApiResponse<Void> deleteComment(String postId, String commentId, String currentUsername) {
        // Buscar el comentario
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comentario", "id", commentId));

        // Validar que el comentario pertenece al post especificado
        if (!comment.getPostId().equals(postId)) {
            throw new ResourceNotFoundException("Comentario", "postId", postId);
        }

        // Buscar el usuario actual
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "username", currentUsername));

        // Verificar permisos: solo el autor o un ADMIN puede eliminar
        if (!comment.getAuthorId().equals(currentUser.getId()) && !"ADMIN".equals(currentUser.getRole())) {
            throw new UnauthorizedAccessException("comentario", "eliminar");
        }

        // Eliminar el comentario
        commentRepository.delete(comment);

        return ApiResponse.success("Comentario eliminado exitosamente", null);
    }

    // Método auxiliar para mapear Comment a CommentResponseDTO
    private CommentResponseDTO mapToDTO(Comment comment) {
        return CommentResponseDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .authorId(comment.getAuthorId())
                .postId(comment.getPostId())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
