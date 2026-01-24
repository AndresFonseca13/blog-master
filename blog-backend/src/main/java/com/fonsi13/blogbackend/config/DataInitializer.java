package com.fonsi13.blogbackend.config;

import com.fonsi13.blogbackend.models.User;
import com.fonsi13.blogbackend.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(UserRepository repository, PasswordEncoder passwordEncoder) {
        return args -> {
            // SOLUCIÓN: Solo borramos y creamos si NO hay usuarios
            if (repository.count() == 0) {

                User testUser = new User();
                testUser.setUsername("admin_fonsi");
                testUser.setEmail("admin@blog.com");
                testUser.setPassword(passwordEncoder.encode("admin123")); // ¡Importante encriptar!
                testUser.setRole("ADMIN");

                repository.save(testUser);
                System.out.println("✅ Usuario ADMIN creado por defecto");

            } else {
                System.out.println("ℹ️ La base de datos ya tiene usuarios, saltando inicialización.");
            }
        };
    }

}
