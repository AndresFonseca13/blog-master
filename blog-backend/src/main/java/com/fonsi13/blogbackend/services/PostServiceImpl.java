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
import com.fonsi13.blogbackend.repositories.PostRepository;
import com.fonsi13.blogbackend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService{

    private final PostRepository postRepository;
    private final UserRepository userRepository;


    @Override
    public ApiResponse<PostResponseDTO> createPost(PostCreateRequest request, String currentUsername) {
        //Buscar el usuario en la BD
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));

        // Generar el Slug a partir del título
        String slug = generateSlug(request.getTitle());

        //Crear la entidad post
        Post post = Post.builder()
                .title(request.getTitle())
                .slug(slug)
                .content(request.getContent())
                .summary(request.getSummary())
                .coverImage(request.getCoverImage())
                .images(request.getImages())
                .videoUrls(request.getVideoUrls())
                .topics(request.getTopics())
                .authorId(currentUser.getId())
                .status(PostStatus.PUBLISHED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Guardar en Mongo
        Post savedPost = postRepository.save(post);

        // Mapear a ResponseDTO
        PostResponseDTO response = mapToDTO(savedPost);

        return ApiResponse.success("Post creado exitosamente", response);
    }

    @Override
    public ApiResponse<Page<PostResponseDTO>> getAllPosts(Pageable pageable) {
        // 1. Buscar paginado en Mongo
        Page<Post> postsPage = postRepository.findAll(pageable);

        // 2. Mapear cada entidad post a postResponseDTO
        Page<PostResponseDTO> dtoPage = postsPage.map(post -> mapToDTO(post));

        return ApiResponse.success("Posts Obtenidos exitosamente", dtoPage);
    }

    @Override
    public ApiResponse<PostResponseDTO> getPostBySlug(String slug) {
        Post post = postRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "slug", slug));

        PostResponseDTO response = mapToDTO(post);

        return ApiResponse.success("Post encontrado", response);
    }

    @Override
    public ApiResponse<PostResponseDTO> updatePost(String postId, PostUpdateRequest request, String currentUsername) {
        // Buscar el post
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        // Buscar el usuario actual
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));

        // Verificar permisos: solo el autor o un ADMIN puede actualizar
        if (!post.getAuthorId().equals(currentUser.getId()) && !"ADMIN".equals(currentUser.getRole())) {
            throw new UnauthorizedAccessException("post", "modificar");
        }

        // Actualizar solo los campos que no son nulos
        if (request.getTitle() != null) {
            post.setTitle(request.getTitle());
            // Regenerar slug si cambia el título
            post.setSlug(generateSlug(request.getTitle()));
        }
        if (request.getContent() != null) {
            post.setContent(request.getContent());
        }
        if (request.getSummary() != null) {
            post.setSummary(request.getSummary());
        }
        if (request.getCoverImage() != null) {
            post.setCoverImage(request.getCoverImage());
        }
        if (request.getImages() != null) {
            post.setImages(request.getImages());
        }
        if (request.getVideoUrls() != null) {
            post.setVideoUrls(request.getVideoUrls());
        }
        if (request.getTopics() != null) {
            post.setTopics(request.getTopics());
        }
        if (request.getStatus() != null) {
            post.setStatus(request.getStatus());
        }

        // Actualizar fecha de modificación
        post.setUpdatedAt(LocalDateTime.now());

        // Guardar cambios
        Post updatedPost = postRepository.save(post);

        return ApiResponse.success("Post actualizado exitosamente", mapToDTO(updatedPost));
    }

    @Override
    public ApiResponse<Void> deletePost(String postId, String currentUsername) {
        // Buscar el post
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        // Buscar el usuario actual
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));

        // Verificar permisos: solo el autor o un ADMIN puede eliminar
        if (!post.getAuthorId().equals(currentUser.getId()) && !"ADMIN".equals(currentUser.getRole())) {
            throw new UnauthorizedAccessException("post", "eliminar");
        }

        // Eliminar el post
        postRepository.delete(post);

        return ApiResponse.success("Post eliminado exitosamente", null);
    }

    // Método utilitario para convertir "Hola Mundo" en "hola-mundo"
    private String generateSlug(String title) {
        String nowhitespace = Pattern.compile("[\\s]").matcher(title).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = Pattern.compile("[^\\w-]").matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH) + "-" + System.currentTimeMillis(); // Agregamos tiempo para evitar duplicados
    }

    // Método auxiliar para no repetir código
    private PostResponseDTO mapToDTO(Post post) {
        return PostResponseDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .slug(post.getSlug())
                .content(post.getContent())
                .summary(post.getSummary())
                .authorId(post.getAuthorId())
                .topics(post.getTopics())
                .status(post.getStatus())
                .createdAt(post.getCreatedAt())
                // --- NUEVOS CAMPOS ---
                .coverImage(post.getCoverImage())
                .images(post.getImages())
                .videoUrls(post.getVideoUrls())
                // ---------------------
                .build();
    }
}
