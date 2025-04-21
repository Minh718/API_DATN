package com.shop.fashion.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class FileUploadUtil {

    private static final String UPLOAD_DIR = "uploads/"; // Set your desired upload directory

    public static String saveImage(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("Image file is required");
            }

            validateFile(file);

            // Create unique image name
            String imageName = System.currentTimeMillis() + "_" + Objects.requireNonNull(file.getOriginalFilename());
            Path path = Paths.get(UPLOAD_DIR + imageName);

            // Create directories if they do not exist
            Files.createDirectories(path.getParent());

            // Save the file to the directory
            Files.write(path, file.getBytes());

            return imageName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store image file", e);
        }
    }

    private static void validateFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !(contentType.startsWith("image/"))) {
            throw new IllegalArgumentException("Invalid file type. Only image files are allowed.");
        }
    }
}
