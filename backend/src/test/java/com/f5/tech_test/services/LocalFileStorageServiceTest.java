package com.f5.tech_test.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.f5.tech_test.services.LocalFileStorageService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LocalFileStorageServiceTest {

    @TempDir
    Path tempDir;

    private LocalFileStorageService fileStorageService;
    private MockMultipartFile testFile;

    @BeforeEach
    void setUp() {
        fileStorageService = new LocalFileStorageService(tempDir.toString());
        testFile = new MockMultipartFile(
            "image",
            "test.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );
    }

    @Test
    void storeFile_ShouldSaveFileAndReturnUrl() throws IOException {
        // Act
        String url = fileStorageService.storeFile(testFile);

        // Assert
        assertNotNull(url);
        assertTrue(url.startsWith("/uploads/"));
        assertTrue(url.endsWith(".jpg"));

        Path savedFile = tempDir.resolve(url.substring("/uploads/".length()));
        assertTrue(Files.exists(savedFile));
        assertEquals("test image content", new String(Files.readAllBytes(savedFile)));
    }

    @Test
    void deleteFile_WithExistingFile_ShouldDeleteFile() throws IOException {
        // Arrange
        String url = fileStorageService.storeFile(testFile);
        String filename = url.substring("/uploads/".length());
        Path filePath = tempDir.resolve(filename);

        // Act
        boolean result = fileStorageService.deleteFile(url);

        // Assert
        assertTrue(result);
        assertFalse(Files.exists(filePath));
    }

    @Test
    void deleteFile_WithNonExistingFile_ShouldReturnFalse() {
        // Act
        boolean result = fileStorageService.deleteFile("/uploads/nonexistent.jpg");

        // Assert
        assertFalse(result);
    }

    @Test
    void getAllFiles_ShouldReturnListOfFileUrls() throws IOException {
        // Arrange
        String url1 = fileStorageService.storeFile(testFile);
        String url2 = fileStorageService.storeFile(new MockMultipartFile(
            "image2",
            "test2.jpg",
            "image/jpeg",
            "test image content 2".getBytes()
        ));

        // Act
        List<String> urls = fileStorageService.getAllFiles();

        // Assert
        assertNotNull(urls);
        assertEquals(2, urls.size());
        assertTrue(urls.contains(url1));
        assertTrue(urls.contains(url2));
    }
} 