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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentServiceImpl Tests")
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentServiceImpl commentService;

    private User commentAuthor;
    private User adminUser;
    private User otherUser;
    private Comment testComment;

    @BeforeEach
    void setUp() {
        commentAuthor = User.builder()
                .id("user-123")
                .username("commenter")
                .role("USER")
                .build();

        adminUser = User.builder()
                .id("admin-123")
                .username("admin")
                .role("ADMIN")
                .build();

        otherUser = User.builder()
                .id("other-123")
                .username("other")
                .role("USER")
                .build();

        testComment = Comment.builder()
                .id("comment-123")
                .content("This is a test comment")
                .postId("post-123")
                .authorId("user-123")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("CS-01 to CS-02: Create Comment")
    class CreateCommentTests {

        @Test
        @DisplayName("CS-01: Should create comment on existing post")
        void shouldCreateCommentOnExistingPost() {
            // Arrange
            CommentCreateRequest request = new CommentCreateRequest();
            request.setContent("Great post!");

            when(postRepository.existsById("post-123")).thenReturn(true);
            when(userRepository.findByUsername("commenter")).thenReturn(Optional.of(commentAuthor));
            when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

            // Act
            ApiResponse<CommentResponseDTO> response = commentService.createComment("post-123", request, "commenter");

            // Assert
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getMessage()).isEqualTo("Comentario agregado");
            assertThat(response.getData()).isNotNull();
            assertThat(response.getData().getPostId()).isEqualTo("post-123");
            assertThat(response.getData().getAuthorId()).isEqualTo("user-123");

            verify(commentRepository).save(any(Comment.class));
        }

        @Test
        @DisplayName("CS-02: Should throw ResourceNotFoundException when post does not exist")
        void shouldThrowWhenPostNotFound() {
            // Arrange
            CommentCreateRequest request = new CommentCreateRequest();
            request.setContent("Comment on missing post");

            when(postRepository.existsById("non-existent")).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> commentService.createComment("non-existent", request, "commenter"))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(commentRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("CS-03: Get Comments by Post")
    class GetCommentsTests {

        @Test
        @DisplayName("CS-03: Should return paginated comments for a post")
        void shouldReturnPaginatedComments() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Comment comment2 = Comment.builder()
                    .id("comment-456")
                    .content("Another comment")
                    .postId("post-123")
                    .authorId("other-123")
                    .createdAt(LocalDateTime.now())
                    .build();

            Page<Comment> commentsPage = new PageImpl<>(List.of(testComment, comment2));
            when(commentRepository.findByPostId("post-123", pageable)).thenReturn(commentsPage);

            // Act
            ApiResponse<Page<CommentResponseDTO>> response = commentService.getCommentByPost("post-123", pageable);

            // Assert
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getData().getContent()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("CS-04 to CS-07: Update Comment")
    class UpdateCommentTests {

        @Test
        @DisplayName("CS-04: Should update comment as author")
        void shouldUpdateCommentAsAuthor() {
            // Arrange
            CommentUpdateRequest request = new CommentUpdateRequest();
            request.setContent("Updated comment");

            Comment updatedComment = Comment.builder()
                    .id("comment-123")
                    .content("Updated comment")
                    .postId("post-123")
                    .authorId("user-123")
                    .createdAt(testComment.getCreatedAt())
                    .updatedAt(LocalDateTime.now())
                    .build();

            when(commentRepository.findById("comment-123")).thenReturn(Optional.of(testComment));
            when(userRepository.findByUsername("commenter")).thenReturn(Optional.of(commentAuthor));
            when(commentRepository.save(any(Comment.class))).thenReturn(updatedComment);

            // Act
            ApiResponse<CommentResponseDTO> response = commentService.updateComment("post-123", "comment-123", request, "commenter");

            // Assert
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getData().getContent()).isEqualTo("Updated comment");
        }

        @Test
        @DisplayName("CS-05: Should update comment as ADMIN")
        void shouldUpdateCommentAsAdmin() {
            // Arrange
            CommentUpdateRequest request = new CommentUpdateRequest();
            request.setContent("Admin-updated comment");

            when(commentRepository.findById("comment-123")).thenReturn(Optional.of(testComment));
            when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
            when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            ApiResponse<CommentResponseDTO> response = commentService.updateComment("post-123", "comment-123", request, "admin");

            // Assert
            assertThat(response.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("CS-06: Should throw UnauthorizedAccessException when other user updates comment")
        void shouldThrowWhenOtherUserUpdates() {
            // Arrange
            CommentUpdateRequest request = new CommentUpdateRequest();
            request.setContent("Unauthorized update");

            when(commentRepository.findById("comment-123")).thenReturn(Optional.of(testComment));
            when(userRepository.findByUsername("other")).thenReturn(Optional.of(otherUser));

            // Act & Assert
            assertThatThrownBy(() -> commentService.updateComment("post-123", "comment-123", request, "other"))
                    .isInstanceOf(UnauthorizedAccessException.class);
        }

        @Test
        @DisplayName("CS-07: Should throw ResourceNotFoundException when comment does not belong to post")
        void shouldThrowWhenCommentNotInPost() {
            // Arrange
            CommentUpdateRequest request = new CommentUpdateRequest();
            request.setContent("Mismatched post");

            when(commentRepository.findById("comment-123")).thenReturn(Optional.of(testComment));

            // Act & Assert
            assertThatThrownBy(() -> commentService.updateComment("different-post", "comment-123", request, "commenter"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("CS-08: Delete Comment")
    class DeleteCommentTests {

        @Test
        @DisplayName("CS-08: Should delete comment as author")
        void shouldDeleteCommentAsAuthor() {
            // Arrange
            when(commentRepository.findById("comment-123")).thenReturn(Optional.of(testComment));
            when(userRepository.findByUsername("commenter")).thenReturn(Optional.of(commentAuthor));

            // Act
            ApiResponse<Void> response = commentService.deleteComment("post-123", "comment-123", "commenter");

            // Assert
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getMessage()).isEqualTo("Comentario eliminado exitosamente");

            verify(commentRepository).delete(testComment);
        }
    }
}
