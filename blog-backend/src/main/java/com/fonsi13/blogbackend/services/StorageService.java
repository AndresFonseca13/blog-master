package com.fonsi13.blogbackend.services;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String uploadImage(MultipartFile file);

    void deleteImage(String imageUrl);
}
