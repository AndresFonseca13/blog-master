package com.fonsi13.blogbackend.services;

import com.fonsi13.blogbackend.dto.ApiResponse;
import com.fonsi13.blogbackend.dto.PostCreateRequest;
import com.fonsi13.blogbackend.dto.PostResponseDTO;
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

        // Generar el Slug a partir del titulo
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

        // Convertir a responseDTO
        PostResponseDTO response = PostResponseDTO.builder()
                .id(savedPost.getId())
                .title(savedPost.getTitle())
                .slug(savedPost.getSlug())
                .content(savedPost.getContent())
                .summary(savedPost.getSummary())
                .coverImage(request.getCoverImage())
                .images(request.getImages())
                .videoUrls(request.getVideoUrls())
                .authorId(savedPost.getAuthorId())
                .topics(savedPost.getTopics())
                .status(savedPost.getStatus())
                .createdAt(savedPost.getCreatedAt())
                .build();

        return ApiResponse.success("Post creado exitosamente", response);
    }

    @Override
    public ApiResponse<Page<PostResponseDTO>> getAllPosts(Pageable pageable) {
        // 1. Buscar paginado en Mongo
        Page<Post> postsPage = postRepository.findAll(pageable);

        // 2. Mapear cada entidad post a postResponseDTO
        Page<PostResponseDTO> dtoPage = postsPage.map(post -> PostResponseDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .slug(post.getSlug())
                .content(post.getContent()) // Ojo: En un listado real, quizás quieras cortar esto a 200 caracteres
                .summary(post.getSummary())
                .coverImage(post.getCoverImage())
                .images(post.getImages())
                .videoUrls(post.getVideoUrls())
                .authorId(post.getAuthorId())
                .topics(post.getTopics())
                .status(post.getStatus())
                .createdAt(post.getCreatedAt())
                .build());

        return ApiResponse.success("Posts Obtenidos exitosamente", dtoPage);
    }

    @Override
    public ApiResponse<PostResponseDTO> getPostBySlug(String slug) {
        Post post = postRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Post no encontrado"));

        PostResponseDTO response = PostResponseDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .slug(post.getSlug())
                .content(post.getContent())
                .summary(post.getSummary())
                .coverImage(post.getCoverImage())
                .images(post.getImages())
                .videoUrls(post.getVideoUrls())
                .authorId(post.getAuthorId())
                .topics(post.getTopics())
                .status(post.getStatus())
                .createdAt(post.getCreatedAt())
                .build();

        return ApiResponse.success("Post encontrado", response);
    }

    // Método utilitario para convertir "Hola Mundo" en "hola-mundo"
    private String generateSlug(String title) {
        String nowhitespace = Pattern.compile("[\\s]").matcher(title).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = Pattern.compile("[^\\w-]").matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH) + "-" + System.currentTimeMillis(); // Agregamos tiempo para evitar duplicados
    }
}
