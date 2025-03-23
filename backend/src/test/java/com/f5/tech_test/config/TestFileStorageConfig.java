package com.f5.tech_test.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.TestPropertySource;

@Configuration
@Primary
@Profile("test")
@TestPropertySource(properties = "file.upload-dir=./test-uploads")
public class TestFileStorageConfig extends FileStorageConfig {
    private final String testUploadDir;

    public TestFileStorageConfig(@Value("${file.upload-dir:./test-uploads}") String testUploadDir) {
        this.testUploadDir = testUploadDir;
    }

    @Override
    public String getUploadDir() {
        return testUploadDir;
    }
} 