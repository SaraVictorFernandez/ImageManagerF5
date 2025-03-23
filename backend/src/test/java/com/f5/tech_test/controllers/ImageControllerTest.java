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
import com.f5.tech_test.dto.ImageDTO;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;


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
        ImageDTO expectedDTO = new ImageDTO();
        expectedDTO.setId(1L);
        expectedDTO.setUrl("http://example.com/images/test.jpg");
        when(imageService.uploadImage(any(), any(), any())).thenReturn(expectedDTO);

        // Act & Assert
        mockMvc.perform(multipart("/api/images")
                .file(validImage)
                .param("title", "Test Title")
                .param("description", "Test Description"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value(expectedDTO.getUrl()));
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
        doThrow(new InvalidImageException("Invalid file type")).when(imageService).uploadImage(any(), any(), any());

        // Act & Assert
        mockMvc.perform(multipart("/api/images")
                .file(invalidFile))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteImage_WithExistingImage_ShouldReturnNoContent() throws Exception {
        // Arrange
        Long imageId = 1L;

        // Act & Assert
        mockMvc.perform(delete("/api/images/{id}", imageId))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteImage_WithNonExistingImage_ShouldReturnNotFound() throws Exception {
        // Arrange
        Long imageId = 1L;
        doThrow(new ImageNotFoundException("Image not found")).when(imageService).deleteImage(imageId);

        // Act & Assert
        mockMvc.perform(delete("/api/images/{id}", imageId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllImages_ShouldReturnListOfImageDTOs() throws Exception {
        // Arrange
        ImageDTO image1 = new ImageDTO();
        image1.setId(1L);
        image1.setUrl("http://example.com/images/image1.jpg");
        ImageDTO image2 = new ImageDTO();
        image2.setId(2L);
        image2.setUrl("http://example.com/images/image2.jpg");
        List<ImageDTO> expectedImages = Arrays.asList(image1, image2);
        when(imageService.getAllImages()).thenReturn(expectedImages);

        // Act & Assert
        mockMvc.perform(get("/api/images"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].url").value(image1.getUrl()))
                .andExpect(jsonPath("$[1].url").value(image2.getUrl()));
    }

    @Test
    void getImageById_ShouldReturnImageDTO() throws Exception {
        // Arrange
        Long imageId = 1L;
        ImageDTO expectedDTO = new ImageDTO();
        expectedDTO.setId(imageId);
        expectedDTO.setUrl("http://example.com/images/test.jpg");
        when(imageService.getImageById(imageId)).thenReturn(expectedDTO);

        // Act & Assert
        mockMvc.perform(get("/api/images/{id}", imageId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(imageId))
                .andExpect(jsonPath("$.url").value(expectedDTO.getUrl()));
    }

    @Test
    void getImageById_WithNonExistingImage_ShouldThrowException() throws Exception {
        // Arrange
        Long imageId = 1L;
        when(imageService.getImageById(imageId)).thenThrow(new ImageNotFoundException("Image not found"));

        // Act & Assert
        mockMvc.perform(get("/api/images/{id}", imageId))
                .andExpect(status().isNotFound());
    }
} 