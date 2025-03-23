package com.f5.tech_test.controllers;

import com.f5.tech_test.exceptions.ImageNotFoundException;
import com.f5.tech_test.exceptions.InvalidImageException;
import com.f5.tech_test.services.ImageService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/images")
public class ImageController {
    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("image") MultipartFile file) throws IOException {
        String imageUrl = imageService.uploadImage(file);
        return ResponseEntity.ok(Map.of("url", imageUrl));
    }

    @DeleteMapping("/{fileName}")
    public ResponseEntity<Void> deleteImage(@PathVariable("fileName") String fileName) {
        String imageUrl = "/uploads/" + fileName;
        imageService.deleteImage(imageUrl);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Map<String, List<String>>> getAllImages() {
        List<String> urls = imageService.getAllImages();
        return ResponseEntity.ok(Map.of("urls", urls));
    }

    @ExceptionHandler(InvalidImageException.class)
    public ResponseEntity<Map<String, String>> handleInvalidImageException(InvalidImageException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(ImageNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleImageNotFoundException(ImageNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
    }
} 