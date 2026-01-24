package com.fonsi13.blogbackend.repositories;

import com.fonsi13.blogbackend.models.Post;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends MongoRepository<Post, String> {

    List<Post> findByAuthorId(String authorId);
    // Para buscar por slug
    Optional<Post> findBySlug(String slug);
}
