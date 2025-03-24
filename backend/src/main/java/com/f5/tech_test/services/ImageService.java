package com.f5.tech_test.services;

import com.f5.tech_test.config.FileStorageConfig;
import com.f5.tech_test.dto.ImageDTO;
import com.f5.tech_test.entities.Image;
import com.f5.tech_test.entities.User;
import com.f5.tech_test.exceptions.ImageNotFoundException;
import com.f5.tech_test.exceptions.InvalidImageException;
import com.f5.tech_test.mappers.ImageMapper;
import com.f5.tech_test.repositories.ImageRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

@Service
@Transactional
public class ImageService {

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
        "image/jpeg",
        "image/png",
        "image/gif"
    );

    private final FileStorageService fileStorageService;
    private final ImageRepository imageRepository;
    private final ImageMapper imageMapper;
    private final FileStorageConfig fileStorageConfig;

    public ImageService(FileStorageService fileStorageService,
                       ImageRepository imageRepository,
                       ImageMapper imageMapper,
                       FileStorageConfig fileStorageConfig) {
        this.fileStorageService = fileStorageService;
        this.imageRepository = imageRepository;
        this.imageMapper = imageMapper;
        this.fileStorageConfig = fileStorageConfig;
    }

    @Transactional
    public ImageDTO uploadImage(MultipartFile file, String title, String description) throws IOException, InvalidImageException {
        validateImage(file);
        
        // Get the current authenticated user
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        // Save the file
        String filename = fileStorageService.storeFile(file);

        // Create and save the image entity
        Image image = new Image();
        image.setFilename(filename);
        image.setOriginalFilename(file.getOriginalFilename());
        image.setContentType(file.getContentType());
        image.setFileSize(file.getSize());
        image.setTitle(title);
        image.setDescription(description);
        image.setUser(currentUser);

        // Image dimension extraction
        try {
            extractImageDimensions(file, image);
        } catch (Exception e) {
            // do nothing
        }

        Image savedImage = imageRepository.save(image);
        return imageMapper.toDTO(savedImage, fileStorageConfig.getBaseUrl());
    }

    @Transactional
    public void deleteImage(Long id) throws IOException {
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new ImageNotFoundException("Image not found with id: " + id));
        
        // Check if the current user owns the image
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!image.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("You can only delete your own images");
        }
        
        fileStorageService.deleteFile(image.getFilename());
        imageRepository.delete(image);
    }

    @Transactional(readOnly = true)
    public List<ImageDTO> getAllImages() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Image> images = imageRepository.findByUser(currentUser);
        return images.stream()
                .map(image -> imageMapper.toDTO(image, fileStorageConfig.getBaseUrl()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ImageDTO getImageById(Long id) {
        return imageRepository.findById(id)
                .map(image -> imageMapper.toDTO(image, fileStorageConfig.getBaseUrl()))
                .orElseThrow(() -> new ImageNotFoundException("Image not found with id: " + id));
    }

    @Transactional
    public ImageDTO updateImage(Long id, MultipartFile file, String title, String description) throws IOException {
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new ImageNotFoundException("Image not found with id: " + id));
        
        // Check if the current user owns the image
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!image.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("You can only update your own images");
        }
        
        String oldImageName = null;
        
        // Change the image entity to the new image
        if(file != null) {
            // Store the new image
            String newImageName = fileStorageService.storeFile(file);
            // Save old image name for deletion at end of method
            oldImageName = image.getFilename();

            // update entitiy
            image.setFilename(newImageName);
        }
        if(title != null) {
            image.setTitle(title);
        }
        if(description != null) {
            image.setDescription(description);
        }
        // save the updated entity
        Image updatedImage = imageRepository.save(image);
        
        if(file != null && oldImageName != null) {
            // Delete the old image
            fileStorageService.deleteFile(oldImageName);
        }

        return imageMapper.toDTO(updatedImage, fileStorageConfig.getBaseUrl());
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

    private void extractImageDimensions(MultipartFile file, Image image) {
        try {
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
            image.setWidth(bufferedImage.getWidth());
            image.setHeight(bufferedImage.getHeight());
        } catch (Exception e) {
            throw new InvalidImageException("Failed to extract image dimensions");
        }
    }
    
} 