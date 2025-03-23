package com.f5.tech_test.services;

import com.f5.tech_test.config.TestFileStorageConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

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
        TestFileStorageConfig config = new TestFileStorageConfig(tempDir.toString());
        fileStorageService = new LocalFileStorageService(config);
        testFile = new MockMultipartFile(
            "image",
            "test.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );
    }

    @Test
    void storeFile_ShouldSaveFileAndReturnName() throws IOException {
        // Act
        String name = fileStorageService.storeFile(testFile);

        // Assert
        assertNotNull(name);
        assertTrue(name.endsWith(".jpg"));

        Path savedFile = tempDir.resolve(name);
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
    void getAllFiles_ShouldReturnListOfFiles() throws IOException {
        // Arrange
        String name1 = fileStorageService.storeFile(testFile);
        String name2 = fileStorageService.storeFile(new MockMultipartFile(
            "image2",
            "test2.jpg",
            "image/jpeg",
            "test image content 2".getBytes()
        ));

        // Act
        List<String> names = fileStorageService.getAllFiles();

        // Assert
        assertNotNull(names);
        assertEquals(2, names.size());
        assertTrue(names.contains(name1));
        assertTrue(names.contains(name2));
    }
} 