package com.fonsi13.blogbackend.repositories;

import com.fonsi13.blogbackend.models.PasswordResetToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends MongoRepository<PasswordResetToken, String> {

    Optional<PasswordResetToken> findByToken(String token);

    List<PasswordResetToken> findByUserId(String userId);

    void deleteByExpiryDateBefore(LocalDateTime dateTime);

    void deleteByUserId(String userId);
}
