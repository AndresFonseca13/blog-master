package com.fonsi13.blogbackend.config;

import com.fonsi13.blogbackend.models.User;
import com.fonsi13.blogbackend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

    @Value("${admin.default.username:admin_fonsi}")
    private String adminDefaultUsername;

    @Value("${admin.default.email:admin@blog.com}")
    private String adminDefaultEmail;

    @Value("${admin.default.password:}")
    private String adminDefaultPassword;

    @Bean
    CommandLineRunner initDatabase(UserRepository repository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Solo creamos el usuario admin si NO hay usuarios en la base de datos
            if (repository.count() == 0) {

                // Validar que la variable de entorno esté configurada
                if (adminDefaultPassword == null || adminDefaultPassword.isBlank()) {
                    System.err.println("ERROR: La variable de entorno ADMIN_DEFAULT_PASSWORD no está configurada.");
                    System.err.println("Por favor, configure la variable antes de iniciar la aplicación.");
                    throw new IllegalStateException(
                            "La variable de entorno ADMIN_DEFAULT_PASSWORD es requerida para la primera ejecución");
                }

                User adminUser = new User();
                adminUser.setUsername(adminDefaultUsername);
                adminUser.setEmail(adminDefaultEmail);
                adminUser.setPassword(passwordEncoder.encode(adminDefaultPassword));
                adminUser.setRole("ADMIN");
                adminUser.setCreatedAt(LocalDateTime.now());

                repository.save(adminUser);
                System.out.println("Usuario ADMIN creado exitosamente con username: " + adminDefaultUsername);

            } else {
                System.out.println("La base de datos ya tiene usuarios, saltando inicialización del admin.");
            }
        };
    }

}
