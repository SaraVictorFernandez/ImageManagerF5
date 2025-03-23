package com.f5.tech_test.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.f5.tech_test.services.ImageService;
import com.f5.tech_test.controllers.ImageController;
import com.f5.tech_test.exceptions.ImageNotFoundException;
import com.f5.tech_test.exceptions.InvalidImageException;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ImageControllerTest {

    @Mock
    private ImageService imageService;

    @InjectMocks
    private ImageController imageController;

    private MockMvc mockMvc;
    private MockMultipartFile validImage;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(imageController).build();
        validImage = new MockMultipartFile(
            "image",
            "test.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "test image content".getBytes()
        );
    }

    @Test
    void uploadImage_WithValidImage_ShouldReturnImageUrl() throws Exception {
        // Arrange
        String expectedUrl = "http://example.com/images/test.jpg";
        when(imageService.uploadImage(any())).thenReturn(expectedUrl);

        // Act & Assert
        mockMvc.perform(multipart("/api/images")
                .file(validImage))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value(expectedUrl));
    }

    @Test
    void uploadImage_WithInvalidFile_ShouldReturnBadRequest() throws Exception {
        // Arrange
        MockMultipartFile invalidFile = new MockMultipartFile(
            "image",
            "test.txt",
            MediaType.TEXT_PLAIN_VALUE,
            "test content".getBytes()
        );
        doThrow(new InvalidImageException("Invalid file type")).when(imageService).uploadImage(any());

        // Act & Assert
        mockMvc.perform(multipart("/api/images")
                .file(invalidFile))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteImage_WithExistingImage_ShouldReturnNoContent() throws Exception {
        // Arrange
        String imageUrl = "http://example.com/images/test.jpg";

        // Act & Assert
        mockMvc.perform(delete("/api/images")
                .param("url", imageUrl))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteImage_WithNonExistingImage_ShouldReturnNotFound() throws Exception {
        // Arrange
        String imageUrl = "http://example.com/images/nonexistent.jpg";
        doThrow(new ImageNotFoundException("Image not found")).when(imageService).deleteImage(imageUrl);

        // Act & Assert
        mockMvc.perform(delete("/api/images")
                .param("url", imageUrl))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllImages_ShouldReturnListOfImageUrls() throws Exception {
        // Arrange
        List<String> expectedUrls = Arrays.asList(
            "http://example.com/images/image1.jpg",
            "http://example.com/images/image2.jpg"
        );
        when(imageService.getAllImages()).thenReturn(expectedUrls);

        // Act & Assert
        mockMvc.perform(get("/api/images"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.urls").isArray())
                .andExpect(jsonPath("$.urls.length()").value(2))
                .andExpect(jsonPath("$.urls[0]").value(expectedUrls.get(0)))
                .andExpect(jsonPath("$.urls[1]").value(expectedUrls.get(1)));
    }
} 