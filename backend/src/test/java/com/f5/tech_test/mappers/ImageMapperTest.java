package com.f5.tech_test.mappers;

import com.f5.tech_test.dto.ImageDTO;
import com.f5.tech_test.entities.Image;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)

public class ImageMapperTest {
    @InjectMocks
    private ImageMapper imageMapper;

    private Image testImage;
    private ImageDTO testImageDTO;
    private static final String BASE_URL = "http://localhost:8080/uploads";

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        
        testImage = new Image();
        testImage.setId(1L);
        testImage.setFilename("test.jpg");
        testImage.setOriginalFilename("original.jpg");
        testImage.setContentType("image/jpeg");
        testImage.setFileSize(1000L);
        testImage.setWidth(800);
        testImage.setHeight(600);
        testImage.setTitle("Test Image");
        testImage.setDescription("Test Description");
        testImage.setUploadDate(now);
        testImage.setLastModifiedDate(now);

        testImageDTO = new ImageDTO();
        testImageDTO.setId(1L);
        testImageDTO.setFilename("test.jpg");
        testImageDTO.setOriginalFilename("original.jpg");
        testImageDTO.setContentType("image/jpeg");
        testImageDTO.setFileSize(1000L);
        testImageDTO.setWidth(800);
        testImageDTO.setHeight(600);
        testImageDTO.setTitle("Test Image");
        testImageDTO.setDescription("Test Description");
        testImageDTO.setUploadDate(now);
        testImageDTO.setLastModifiedDate(now);
        testImageDTO.setUrl(BASE_URL + "/test.jpg");
    }

    @Test
    void toDTO_WithValidImage_ShouldReturnCorrectDTO() {
        // Act
        ImageDTO result = imageMapper.toDTO(testImage, BASE_URL);

        // Assert
        assertNotNull(result);
        assertEquals(testImage.getId(), result.getId());
        assertEquals(testImage.getFilename(), result.getFilename());
        assertEquals(testImage.getOriginalFilename(), result.getOriginalFilename());
        assertEquals(testImage.getContentType(), result.getContentType());
        assertEquals(testImage.getFileSize(), result.getFileSize());
        assertEquals(testImage.getWidth(), result.getWidth());
        assertEquals(testImage.getHeight(), result.getHeight());
        assertEquals(testImage.getTitle(), result.getTitle());
        assertEquals(testImage.getDescription(), result.getDescription());
        assertEquals(testImage.getUploadDate(), result.getUploadDate());
        assertEquals(testImage.getLastModifiedDate(), result.getLastModifiedDate());
        assertEquals(BASE_URL + "/" + testImage.getFilename(), result.getUrl());
    }

    @Test
    void toDTO_WithNullImage_ShouldReturnNull() {
        // Act
        ImageDTO result = imageMapper.toDTO(null, BASE_URL);

        // Assert
        assertNull(result);
    }

    @Test
    void toDTO_WithNullBaseUrl_ShouldNotSetUrl() {
        // Act
        ImageDTO result = imageMapper.toDTO(testImage, null);

        // Assert
        assertNotNull(result);
        assertNull(result.getUrl());
    }

    @Test
    void toDTO_WithEmptyBaseUrl_ShouldNotSetUrl() {
        // Act
        ImageDTO result = imageMapper.toDTO(testImage, "");

        // Assert
        assertNotNull(result);
        assertNull(result.getUrl());
    }

    @Test
    void toEntity_WithValidDTO_ShouldReturnCorrectEntity() {
        // Act
        Image result = imageMapper.toEntity(testImageDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testImageDTO.getId(), result.getId());
        assertEquals(testImageDTO.getFilename(), result.getFilename());
        assertEquals(testImageDTO.getOriginalFilename(), result.getOriginalFilename());
        assertEquals(testImageDTO.getContentType(), result.getContentType());
        assertEquals(testImageDTO.getFileSize(), result.getFileSize());
        assertEquals(testImageDTO.getWidth(), result.getWidth());
        assertEquals(testImageDTO.getHeight(), result.getHeight());
        assertEquals(testImageDTO.getTitle(), result.getTitle());
        assertEquals(testImageDTO.getDescription(), result.getDescription());
    }

    @Test
    void toEntity_WithNullDTO_ShouldReturnNull() {
        // Act
        Image result = imageMapper.toEntity(null);

        // Assert
        assertNull(result);
    }
}
