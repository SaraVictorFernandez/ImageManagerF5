package com.f5.tech_test.mappers;

import com.f5.tech_test.dto.ImageDTO;
import com.f5.tech_test.entities.Image;
import org.springframework.stereotype.Component;

@Component
public class ImageMapper {
    
    public ImageDTO toDTO(Image image, String baseUrl) {
        if (image == null) {
            return null;
        }

        ImageDTO dto = new ImageDTO();
        dto.setId(image.getId());
        dto.setFilename(image.getFilename());
        dto.setOriginalFilename(image.getOriginalFilename());
        dto.setContentType(image.getContentType());
        dto.setFileSize(image.getFileSize());
        dto.setWidth(image.getWidth());
        dto.setHeight(image.getHeight());
        dto.setTitle(image.getTitle());
        dto.setDescription(image.getDescription());
        dto.setUploadDate(image.getUploadDate());
        dto.setLastModifiedDate(image.getLastModifiedDate());
        
        // Construct the URL for the image
        if (baseUrl != null && !baseUrl.isEmpty()) {
            dto.setUrl(baseUrl + "/" + image.getFilename());
        }

        return dto;
    }

    public Image toEntity(ImageDTO dto) {
        if (dto == null) {
            return null;
        }

        Image image = new Image();
        image.setId(dto.getId());
        image.setFilename(dto.getFilename());
        image.setOriginalFilename(dto.getOriginalFilename());
        image.setContentType(dto.getContentType());
        image.setFileSize(dto.getFileSize());
        image.setWidth(dto.getWidth());
        image.setHeight(dto.getHeight());
        image.setTitle(dto.getTitle());
        image.setDescription(dto.getDescription());

        return image;
    }
} 