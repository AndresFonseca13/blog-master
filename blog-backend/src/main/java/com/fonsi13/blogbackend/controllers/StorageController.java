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

@RestController
@RequestMapping("api/v1/storage")
@RequiredArgsConstructor
public class StorageController {

    private final StorageService storageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadImage(@RequestParam("file")MultipartFile file){
        // Valider que sea una imagen
        if (!file.getContentType().startsWith("image/")){
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Solo se permiten archivos de imagen"));
        }

        String imageUrl = storageService.uploadImage(file);

        return ResponseEntity.ok(ApiResponse.success("Imagen subida correctamente", imageUrl));
    }

}
