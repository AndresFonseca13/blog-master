package com.fonsi13.blogbackend.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "users")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    private String email;
    private String password;
    private String role;
    private LocalDateTime createdAt;

    // Campos para OAuth2
    @Builder.Default
    private AuthProvider provider = AuthProvider.LOCAL;
    private String providerId;
    private String profilePicture;
    @Builder.Default
    private boolean emailVerified = false;
}
