package com.fonsi13.blogbackend.repositories;

import com.fonsi13.blogbackend.models.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends MongoRepository<Comment, String> {

    Page<Comment> findByPostId(String postId, Pageable pageable);

}
