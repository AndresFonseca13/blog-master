package com.fonsi13.blogbackend.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class SupabaseStorageService implements StorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    @Value("${supabase.bucket}")
    private String bucketName;

    private final RestClient restClient;

    public SupabaseStorageService() {
        this.restClient = RestClient.create();
    }

    @Override
    public String uploadImage(MultipartFile file) {
        try {
            // 1. Generar un nombre único para el archivo (evitar sobreescrituras)
            // Ej: "foto-perfil.jpg" -> "foto-perfil-123e4567.jpg"
            String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename()
                    .replace(" ", "-"); // Limpiamos espacios

            // 2. Construir la URL de subida de Supabase
            // POST /storage/v1/object/{bucket}/{path}
            String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + fileName;

            // 3. Hacer la petición POST a Supabase
            restClient.post()
                    .uri(uploadUrl)
                    .header("Authorization", "Bearer " + supabaseKey)
                    .contentType(MediaType.parseMediaType(file.getContentType())) // image/jpeg, etc.
                    .body(file.getBytes()) // Enviamos los bytes crudos
                    .retrieve()
                    .toBodilessEntity();

            // 4. Retornar la URL pública para guardarla en la BD
            // Formato: https://<project>.supabase.co/storage/v1/object/public/<bucket>/<file>
            return supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + fileName;

        } catch (Exception e) {
            throw new RuntimeException("Error al subir imagen a Supabase: " + e.getMessage());
        }
    }
}
