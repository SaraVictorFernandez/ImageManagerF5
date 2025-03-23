package com.f5.tech_test.services;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

public interface FileStorageService {
    String storeFile(MultipartFile file) throws IOException;
    boolean deleteFile(String fileUrl);
    List<String> getAllFiles();
} 