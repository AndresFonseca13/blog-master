package com.fonsi13.blogbackend.services;

import com.fonsi13.blogbackend.dto.ApiResponse;
import com.fonsi13.blogbackend.dto.PostCreateRequest;
import com.fonsi13.blogbackend.dto.PostResponseDTO;
import com.fonsi13.blogbackend.dto.PostUpdateRequest;
import com.fonsi13.blogbackend.exceptions.ResourceNotFoundException;
import com.fonsi13.blogbackend.exceptions.UnauthorizedAccessException;
import com.fonsi13.blogbackend.models.Post;
import com.fonsi13.blogbackend.models.PostStatus;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostServiceImpl Tests")
class PostServiceImplTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StorageService storageService;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private PostServiceImpl postService;

    private User authorUser;
    private User adminUser;
    private User otherUser;
    private Post testPost;
    private PostCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        authorUser = User.builder()
                .id("author-123")
                .username("author")
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

        testPost = Post.builder()
                .id("post-123")
                .title("Test Post")
                .slug("test-post-12345")
                .content("Post content")
                .summary("Post summary")
                .coverImage("https://supabase.co/storage/v1/object/public/blog-images/cover.jpg")
                .images(List.of("https://supabase.co/storage/v1/object/public/blog-images/img1.jpg"))
                .authorId("author-123")
                .topics(List.of("java", "spring"))
                .status(PostStatus.PUBLISHED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createRequest = new PostCreateRequest();
        createRequest.setTitle("Test Post");
        createRequest.setContent("Post content");
        createRequest.setSummary("Post summary");
        createRequest.setTopics(List.of("java", "spring"));
    }

    @Nested
    @DisplayName("PS-01 to PS-02: Create Post")
    class CreatePostTests {

        @Test
        @DisplayName("PS-01: Should create post successfully with valid data")
        void shouldCreatePostSuccessfully() {
            // Arrange
            when(userRepository.findByUsername("author")).thenReturn(Optional.of(authorUser));
            when(postRepository.save(any(Post.class))).thenReturn(testPost);

            // Act
            ApiResponse<PostResponseDTO> response = postService.createPost(createRequest, "author");

            // Assert
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getMessage()).isEqualTo("Post creado exitosamente");
            assertThat(response.getData()).isNotNull();
            assertThat(response.getData().getTitle()).isEqualTo("Test Post");
            assertThat(response.getData().getAuthorId()).isEqualTo("author-123");
            assertThat(response.getData().getStatus()).isEqualTo(PostStatus.PUBLISHED);

            verify(postRepository).save(any(Post.class));
        }

        @Test
        @DisplayName("PS-02: Should generate a valid slug from title")
        void shouldGenerateValidSlug() {
            // Arrange
            when(userRepository.findByUsername("author")).thenReturn(Optional.of(authorUser));
            when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
                Post savedPost = invocation.getArgument(0);
                // Verify the slug was generated properly
                assertThat(savedPost.getSlug()).isNotNull();
                assertThat(savedPost.getSlug()).startsWith("test-post-");
                assertThat(savedPost.getSlug()).matches("test-post-\\d+");
                return savedPost;
            });

            // Act
            postService.createPost(createRequest, "author");

            // Assert
            verify(postRepository).save(any(Post.class));
        }
    }

    @Nested
    @DisplayName("PS-03 to PS-04: Get All Posts")
    class GetAllPostsTests {

        @Test
        @DisplayName("PS-03: Should return only published posts for anonymous user")
        void shouldReturnOnlyPublishedPostsForAnonymous() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Page<Post> postsPage = new PageImpl<>(List.of(testPost));
            when(postRepository.findByStatus(PostStatus.PUBLISHED, pageable)).thenReturn(postsPage);

            // Act
            ApiResponse<Page<PostResponseDTO>> response = postService.getAllPosts(pageable, null);

            // Assert
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getData().getContent()).hasSize(1);

            verify(postRepository).findByStatus(PostStatus.PUBLISHED, pageable);
            verify(postRepository, never()).findAll(pageable);
        }

        @Test
        @DisplayName("PS-04: Should return all posts for ADMIN user")
        void shouldReturnAllPostsForAdmin() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Post draftPost = Post.builder()
                    .id("draft-1")
                    .title("Draft")
                    .status(PostStatus.DRAFT)
                    .authorId("admin-123")
                    .build();
            Page<Post> allPostsPage = new PageImpl<>(List.of(testPost, draftPost));

            when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
            when(postRepository.findAll(pageable)).thenReturn(allPostsPage);

            // Act
            ApiResponse<Page<PostResponseDTO>> response = postService.getAllPosts(pageable, "admin");

            // Assert
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getData().getContent()).hasSize(2);

            verify(postRepository).findAll(pageable);
        }
    }

    @Nested
    @DisplayName("PS-05 to PS-06: Get Post by Slug")
    class GetPostBySlugTests {

        @Test
        @DisplayName("PS-05: Should return post for existing slug")
        void shouldReturnPostForExistingSlug() {
            // Arrange
            when(postRepository.findBySlug("test-post-12345")).thenReturn(Optional.of(testPost));

            // Act
            ApiResponse<PostResponseDTO> response = postService.getPostBySlug("test-post-12345");

            // Assert
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getData().getSlug()).isEqualTo("test-post-12345");
        }

        @Test
        @DisplayName("PS-06: Should throw ResourceNotFoundException for non-existing slug")
        void shouldThrowWhenSlugNotFound() {
            // Arrange
            when(postRepository.findBySlug("non-existent")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> postService.getPostBySlug("non-existent"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("PS-07 to PS-08: Update Post")
    class UpdatePostTests {

        @Test
        @DisplayName("PS-07: Should update post successfully as author")
        void shouldUpdatePostAsAuthor() {
            // Arrange
            PostUpdateRequest updateRequest = new PostUpdateRequest();
            updateRequest.setTitle("Updated Title");
            updateRequest.setContent("Updated content");

            when(postRepository.findById("post-123")).thenReturn(Optional.of(testPost));
            when(userRepository.findByUsername("author")).thenReturn(Optional.of(authorUser));
            when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            ApiResponse<PostResponseDTO> response = postService.updatePost("post-123", updateRequest, "author");

            // Assert
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getData().getTitle()).isEqualTo("Updated Title");
            assertThat(response.getData().getContent()).isEqualTo("Updated content");
        }

        @Test
        @DisplayName("PS-08: Should throw UnauthorizedAccessException when non-author/non-admin updates")
        void shouldThrowWhenNonAuthorUpdates() {
            // Arrange
            PostUpdateRequest updateRequest = new PostUpdateRequest();
            updateRequest.setTitle("Hacked Title");

            when(postRepository.findById("post-123")).thenReturn(Optional.of(testPost));
            when(userRepository.findByUsername("other")).thenReturn(Optional.of(otherUser));

            // Act & Assert
            assertThatThrownBy(() -> postService.updatePost("post-123", updateRequest, "other"))
                    .isInstanceOf(UnauthorizedAccessException.class);
        }
    }

    @Nested
    @DisplayName("PS-09 to PS-10: Delete Post")
    class DeletePostTests {

        @Test
        @DisplayName("PS-09: Should delete post with images and comments as author")
        void shouldDeletePostWithImagesAndComments() {
            // Arrange
            when(postRepository.findById("post-123")).thenReturn(Optional.of(testPost));
            when(userRepository.findByUsername("author")).thenReturn(Optional.of(authorUser));

            // Act
            ApiResponse<Void> response = postService.deletePost("post-123", "author");

            // Assert
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getMessage()).isEqualTo("Post eliminado exitosamente");

            // Verify images were deleted
            verify(storageService).deleteImage(testPost.getCoverImage());
            verify(storageService).deleteImage(testPost.getImages().get(0));

            // Verify comments were deleted
            verify(commentRepository).deleteByPostId("post-123");

            // Verify post was deleted
            verify(postRepository).delete(testPost);
        }

        @Test
        @DisplayName("PS-10: Should throw ResourceNotFoundException when deleting non-existing post")
        void shouldThrowWhenDeletingNonExistingPost() {
            // Arrange
            when(postRepository.findById("non-existent")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> postService.deletePost("non-existent", "author"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
