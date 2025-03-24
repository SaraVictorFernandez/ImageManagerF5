package com.f5.tech_test.services;

import com.f5.tech_test.config.FileStorageConfig;
import com.f5.tech_test.dto.ImageDTO;
import com.f5.tech_test.entities.Image;
import com.f5.tech_test.entities.User;
import com.f5.tech_test.exceptions.ImageNotFoundException;
import com.f5.tech_test.exceptions.InvalidImageException;
import com.f5.tech_test.mappers.ImageMapper;
import com.f5.tech_test.repositories.ImageRepository;
import com.f5.tech_test.services.ImageService;
import com.f5.tech_test.services.FileStorageService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ImageServiceTest {

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private ImageMapper imageMapper;

    @Mock
    private FileStorageConfig fileStorageConfig;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ImageService imageService;

    private MockMultipartFile validImage;
    private MockMultipartFile invalidFile;
    private Image testImage;
    private ImageDTO testImageDTO;
    private User testUser;
    private User otherUser;
    private static final Logger logger = LoggerFactory.getLogger(ImageServiceTest.class);

    @BeforeEach
    void setUp() {
        validImage = new MockMultipartFile(
            "image",
            "test.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );

        invalidFile = new MockMultipartFile(
            "image",
            "test.txt",
            "text/plain",
            "test content".getBytes()
        );

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("otheruser");

        testImage = new Image();
        testImage.setId(1L);
        testImage.setFilename("test.jpg");
        testImage.setOriginalFilename("original.jpg");
        testImage.setContentType("image/jpeg");
        testImage.setFileSize(1000L);
        testImage.setTitle("Test Image");
        testImage.setDescription("Test Description");
        testImage.setUploadDate(LocalDateTime.now());
        testImage.setLastModifiedDate(LocalDateTime.now());
        testImage.setUser(testUser);

        testImageDTO = new ImageDTO();
        testImageDTO.setId(1L);
        testImageDTO.setFilename("test.jpg");
        testImageDTO.setOriginalFilename("original.jpg");
        testImageDTO.setContentType("image/jpeg");
        testImageDTO.setFileSize(1000L);
        testImageDTO.setTitle("Test Image");
        testImageDTO.setDescription("Test Description");
        testImageDTO.setUploadDate(LocalDateTime.now());
        testImageDTO.setLastModifiedDate(LocalDateTime.now());
        testImageDTO.setUrl("http://localhost:8080/uploads/test.jpg");

        when(fileStorageConfig.getBaseUrl()).thenReturn("http://localhost:8080/uploads");
        
        // Setup SecurityContext mock
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
    }

    @Test
    void uploadImage_WithValidImage_ShouldReturnImageDTO() throws IOException {
        // Arrange
        String filename = "test.jpg";
        when(fileStorageService.storeFile(any(MultipartFile.class))).thenReturn(filename);
        when(imageRepository.save(any(Image.class))).thenReturn(testImage);
        when(imageMapper.toDTO(any(Image.class), eq("http://localhost:8080/uploads"))).thenReturn(testImageDTO);

        // Act
        ImageDTO result = imageService.uploadImage(validImage, "Test Title", "Test Description");

        // Assert
        assertNotNull(result);
        assertEquals(testImageDTO.getId(), result.getId());
        assertEquals(testImageDTO.getTitle(), result.getTitle());
        assertEquals(testImageDTO.getDescription(), result.getDescription());
        assertEquals(testImageDTO.getUrl(), result.getUrl());
        
        verify(fileStorageService).storeFile(eq(validImage));
        verify(imageRepository).save(any(Image.class));
        verify(imageMapper).toDTO(any(Image.class), anyString());
    }

    @Test
    void uploadImage_WithInvalidFile_ShouldThrowException() throws IOException {
        // Act & Assert
        assertThrows(InvalidImageException.class, 
            () -> imageService.uploadImage(invalidFile, "Test Title", "Test Description"));
        verify(fileStorageService, never()).storeFile(any());
        verify(imageRepository, never()).save(any(Image.class));
        verify(imageMapper, never()).toDTO(any(), any());
    }

    @Test
    void deleteImage_WithOwnImage_ShouldDeleteSuccessfully() throws IOException {
        // Arrange
        Long imageId = 1L;
        when(imageRepository.findById(anyLong())).thenReturn(Optional.of(testImage));
        
        // Act & Assert
        assertDoesNotThrow(() -> imageService.deleteImage(imageId));
        verify(fileStorageService).deleteFile(testImage.getFilename());
        verify(imageRepository).delete(testImage);
    }

    @Test
    void deleteImage_WithOtherUserImage_ShouldThrowException() throws IOException {
        // Arrange
        Long imageId = 1L;
        Image otherUserImage = new Image();
        otherUserImage.setId(1L);
        otherUserImage.setUser(otherUser);
        when(imageRepository.findById(anyLong())).thenReturn(Optional.of(otherUserImage));
        
        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> imageService.deleteImage(imageId));
        assertEquals("You can only delete your own images", exception.getMessage());
        verify(fileStorageService, never()).deleteFile(anyString());
        verify(imageRepository, never()).delete(any(Image.class));
    }

    @Test
    void deleteImage_WithNonExistingImage_ShouldThrowException() {
        // Arrange
        Long imageId = 1L;
        when(imageRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ImageNotFoundException.class, () -> imageService.deleteImage(imageId));
        verify(fileStorageService, never()).deleteFile(anyString());
        verify(imageRepository, never()).delete(any(Image.class));
    }

    @Test
    void getAllImages_ShouldReturnListOfImageDTOs() {
        // Arrange
        List<Image> images = Arrays.asList(testImage);
        when(imageRepository.findAll()).thenReturn(images);
        when(imageMapper.toDTO(any(Image.class), anyString())).thenReturn(testImageDTO);

        // Act
        List<ImageDTO> result = imageService.getAllImages();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testImageDTO.getId(), result.get(0).getId());
        assertEquals(testImageDTO.getUrl(), result.get(0).getUrl());
        verify(imageRepository).findAll();
        verify(imageMapper).toDTO(any(Image.class), anyString());
    }

    @Test
    void getImageById_ShouldReturnImageDTO() {
        // Arrange
        Long imageId = 1L;
        when(imageRepository.findById(imageId)).thenReturn(Optional.of(testImage));
        when(imageMapper.toDTO(testImage, fileStorageConfig.getBaseUrl())).thenReturn(testImageDTO);

        // Act
        ImageDTO result = imageService.getImageById(imageId);

        // Assert
        assertNotNull(result);
        assertEquals(testImageDTO.getId(), result.getId());
        assertEquals(testImageDTO.getUrl(), result.getUrl());
        verify(imageRepository).findById(anyLong());
        verify(imageMapper).toDTO(any(Image.class), anyString());
    }

    @Test
    void getImageById_WithNonExistingImage_ShouldThrowException() {
        // Arrange
        Long imageId = 1L;
        when(imageRepository.findById(imageId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ImageNotFoundException.class, () -> imageService.getImageById(imageId));
        verify(imageMapper, never()).toDTO(any(), any());
    }

    @Test
    void updateImage_WithOwnImage_ShouldUpdateSuccessfully() throws IOException {
        // Arrange
        Long imageId = 1L;
        when(imageRepository.findById(imageId)).thenReturn(Optional.of(testImage));
        when(imageRepository.save(any(Image.class))).thenReturn(testImage);
        when(imageMapper.toDTO(any(Image.class), anyString())).thenReturn(testImageDTO);

        // Act
        ImageDTO result = imageService.updateImage(imageId, null, "New Title", "New Description");

        // Assert
        assertNotNull(result);
        assertEquals(testImageDTO.getId(), result.getId());
        assertEquals(testImageDTO.getTitle(), result.getTitle());
        assertEquals(testImageDTO.getDescription(), result.getDescription());
        verify(imageRepository).save(any(Image.class));
    }

    @Test
    void updateImage_WithOtherUserImage_ShouldThrowException() throws IOException {
        // Arrange
        Long imageId = 1L;
        Image otherUserImage = new Image();
        otherUserImage.setId(1L);
        otherUserImage.setUser(otherUser);
        when(imageRepository.findById(imageId)).thenReturn(Optional.of(otherUserImage));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> imageService.updateImage(imageId, null, "New Title", "New Description"));
        assertEquals("You can only update your own images", exception.getMessage());
        verify(imageRepository, never()).save(any(Image.class));
    }
} 