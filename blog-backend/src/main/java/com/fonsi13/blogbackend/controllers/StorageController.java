package com.fonsi13.blogbackend.controllers;

import com.fonsi13.blogbackend.dto.ApiResponse;
import com.fonsi13.blogbackend.services.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@RestController
@RequestMapping("api/v1/storage")
@RequiredArgsConstructor
public class StorageController {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    private final StorageService storageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadImage(@RequestParam("file") MultipartFile file) {
        // Validar que el archivo no esté vacío
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("El archivo está vacío"));
        }

        // Validar tamaño máximo (5MB)
        if (file.getSize() > MAX_FILE_SIZE) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("El archivo excede el tamaño máximo permitido (5MB)"));
        }

        // Validar Content-Type (puede ser null si el cliente no lo envía)
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Tipo de archivo no permitido. Solo se permiten: JPG, PNG, GIF, WEBP"));
        }

        // Validar extensión del archivo original
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !hasAllowedExtension(originalFilename)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Extensión de archivo no permitida. Solo se permiten: jpg, jpeg, png, gif, webp"));
        }

        String imageUrl = storageService.uploadImage(file);
        return ResponseEntity.ok(ApiResponse.success("Imagen subida correctamente", imageUrl));
    }

    private boolean hasAllowedExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex == -1) return false;
        String extension = filename.substring(dotIndex + 1).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(extension);
    }
}
