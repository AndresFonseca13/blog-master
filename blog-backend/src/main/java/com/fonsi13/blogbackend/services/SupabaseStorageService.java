package com.fonsi13.blogbackend.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@Slf4j
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

    @Override
    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            log.warn("Se intentó eliminar una imagen con URL nula o vacía");
            return;
        }

        try {
            // Extraer el nombre del archivo de la URL pública
            // URL: https://<project>.supabase.co/storage/v1/object/public/<bucket>/<filename>
            String publicPath = "/storage/v1/object/public/" + bucketName + "/";

            if (!imageUrl.contains(publicPath)) {
                log.warn("La URL no pertenece al bucket configurado: {}", imageUrl);
                return;
            }

            String fileName = imageUrl.substring(imageUrl.indexOf(publicPath) + publicPath.length());

            // Construir la URL de eliminación de Supabase
            // DELETE /storage/v1/object/{bucket}/{path}
            String deleteUrl = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + fileName;

            // Hacer la petición DELETE a Supabase
            restClient.delete()
                    .uri(deleteUrl)
                    .header("Authorization", "Bearer " + supabaseKey)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Imagen eliminada exitosamente de Supabase: {}", fileName);

        } catch (Exception e) {
            // No lanzamos excepción para no bloquear la eliminación del post
            log.error("Error al eliminar imagen de Supabase ({}): {}", imageUrl, e.getMessage());
        }
    }
}
