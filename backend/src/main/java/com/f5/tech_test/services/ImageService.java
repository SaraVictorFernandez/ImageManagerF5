package com.f5.tech_test.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.f5.tech_test.exceptions.ImageNotFoundException;
import com.f5.tech_test.exceptions.InvalidImageException;

import java.io.IOException;
import java.util.List;

@Service
public class ImageService {
    private final FileStorageService fileStorageService;
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
        "image/jpeg",
        "image/png",
        "image/gif"
    );

    public ImageService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    public String uploadImage(MultipartFile file) throws IOException {
        validateImage(file);
        return fileStorageService.storeFile(file);
    }

    public void deleteImage(String imageUrl) {
        if (!fileStorageService.deleteFile(imageUrl)) {
            throw new ImageNotFoundException("Image not found: " + imageUrl);
        }
    }

    public List<String> getAllImages() {
        return fileStorageService.getAllFiles();
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidImageException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new InvalidImageException("Invalid file type. Allowed types: JPEG, PNG, GIF");
        }
    }
} 