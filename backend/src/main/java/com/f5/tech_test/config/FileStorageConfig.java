package com.f5.tech_test.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application.properties")
public class FileStorageConfig {
    
    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;
    
    public String getUploadDir() {
        return uploadDir;
    }
} 