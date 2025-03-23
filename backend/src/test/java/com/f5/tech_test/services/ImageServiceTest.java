package com.f5.tech_test.services;

import com.f5.tech_test.exceptions.ImageNotFoundException;
import com.f5.tech_test.exceptions.InvalidImageException;
import com.f5.tech_test.services.ImageService;
import com.f5.tech_test.services.FileStorageService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private ImageService imageService;

    private MockMultipartFile validImage;
    private MockMultipartFile invalidFile;

    @BeforeEach
    void setUp() {
        validImage = new MockMultipartFile(
            "image",
            "test.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );

        invalidFile = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            "test content".getBytes()
        );
    }

    @Test
    void uploadImage_WithValidImage_ShouldReturnImageUrl() throws IOException {
        // Arrange
        String expectedUrl = "http://example.com/images/test.jpg";
        when(fileStorageService.storeFile(any(MultipartFile.class))).thenReturn(expectedUrl);

        // Act
        String result = imageService.uploadImage(validImage);

        // Assert
        assertNotNull(result);
        assertEquals(expectedUrl, result);
        verify(fileStorageService).storeFile(validImage);
    }

    @Test
    void uploadImage_WithInvalidFile_ShouldThrowException() throws IOException {
        // Act & Assert
        assertThrows(InvalidImageException.class, () -> imageService.uploadImage(invalidFile));
        verify(fileStorageService, never()).storeFile(any());
    }

    @Test
    void deleteImage_WithExistingImage_ShouldDeleteSuccessfully() {
        // Arrange
        String imageUrl = "http://example.com/images/test.jpg";
        when(fileStorageService.deleteFile(anyString())).thenReturn(true);

        // Act & Assert
        assertDoesNotThrow(() -> imageService.deleteImage(imageUrl));
        verify(fileStorageService).deleteFile(imageUrl);
    }

    @Test
    void deleteImage_WithNonExistingImage_ShouldThrowException() {
        // Arrange
        String imageUrl = "http://example.com/images/nonexistent.jpg";
        when(fileStorageService.deleteFile(anyString())).thenReturn(false);

        // Act & Assert
        assertThrows(ImageNotFoundException.class, () -> imageService.deleteImage(imageUrl));
        verify(fileStorageService).deleteFile(imageUrl);
    }

    @Test
    void getAllImages_ShouldReturnListOfImageUrls() {
        // Arrange
        List<String> expectedUrls = Arrays.asList(
            "http://example.com/images/image1.jpg",
            "http://example.com/images/image2.jpg"
        );
        when(fileStorageService.getAllFiles()).thenReturn(expectedUrls);

        // Act
        List<String> result = imageService.getAllImages();

        // Assert
        assertNotNull(result);
        assertEquals(expectedUrls, result);
        verify(fileStorageService).getAllFiles();
    }
} 