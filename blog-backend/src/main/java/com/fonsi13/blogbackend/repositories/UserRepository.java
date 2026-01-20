package com.fonsi13.blogbackend.repositories;

import com.fonsi13.blogbackend.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {
}
