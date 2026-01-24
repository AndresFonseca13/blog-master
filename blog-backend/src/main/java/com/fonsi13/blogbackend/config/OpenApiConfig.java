package com.fonsi13.blogbackend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Blog Backend API",
                version = "1.0",
                description = "Documentación de la API para el Blog Personal",
                contact = @Contact(
                        name = "Fonsi Dev",
                        email = "andresitofonseca13@gmail.com"
                )
        ),
        servers = {
                @Server(
                        description = "Servidor Local",
                        url = "http://localhost:8080"
                )
        }
)
public class OpenApiConfig {
}
