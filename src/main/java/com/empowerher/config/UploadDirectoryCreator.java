package com.empowerher.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class UploadDirectoryCreator {

    @Value("${file.upload-dir:uploads/}")
    private String uploadDir;

    @PostConstruct
    public void createUploadDirectories() {
        try {
            Path uploadPath = Paths.get(uploadDir);
            Path imagesPath = uploadPath.resolve("images");
            Path pdfsPath = uploadPath.resolve("pdfs");

            // Create directories if they don't exist
            Files.createDirectories(imagesPath);
            Files.createDirectories(pdfsPath);

            System.out.println("✅ Upload directories created successfully:");
            System.out.println("📁 Images: " + imagesPath.toAbsolutePath());
            System.out.println("📁 PDFs: " + pdfsPath.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("❌ ERROR creating upload directories: " + e.getMessage());
        }
    }
}