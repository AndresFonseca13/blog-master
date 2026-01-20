package com.fonsi13.blogbackend.config;

import com.fonsi13.blogbackend.models.User;
import com.fonsi13.blogbackend.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(UserRepository repository) {
        return args -> {
            // Limpiamos la colección para no duplicar datos en cada reinicio
            repository.deleteAll();

            // Creamos un usuario de prueba
            User testUser = new User();
            testUser.setUsername("fonsi_dev");
            testUser.setEmail("test@blog.com");
            testUser.setPassword("password_seguro"); // Luego usaremos BCrypt
            testUser.setRole("ADMIN");

            // Guardamos en MongoDB
            repository.save(testUser);

            System.out.println("-----------------------------------------");
            System.out.println("✅ CONEXIÓN EXITOSA: Usuario de prueba creado en Mongo");
            System.out.println("-----------------------------------------");
        };
    }

}
