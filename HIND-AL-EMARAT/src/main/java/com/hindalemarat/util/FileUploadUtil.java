package com.hindalemarat.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Component
public class FileUploadUtil {

    @Value("${file.upload.dir:uploads/products}")
    private String uploadDir;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>();

    static {
        ALLOWED_EXTENSIONS.add("jpg");
        ALLOWED_EXTENSIONS.add("jpeg");
        ALLOWED_EXTENSIONS.add("png");
    }

    public String uploadProductImage(MultipartFile file) throws IOException, IllegalArgumentException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of 5MB");
        }

        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException("File name is invalid");
        }

        String fileExtension = getFileExtension(filename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
            throw new IllegalArgumentException("File type not allowed. Allowed types: jpg, jpeg, png");
        }

        File uploadDirectory = new File(uploadDir);
        if (!uploadDirectory.exists()) {
            Files.createDirectories(uploadDirectory.toPath());
        }

        String uniqueFilename = UUID.randomUUID() + "." + fileExtension;
        Path filePath = Paths.get(uploadDir, uniqueFilename);

        Files.write(filePath, file.getBytes());

        return uniqueFilename;
    }

    public void deleteProductImage(String filename) {
        if (filename == null || filename.isEmpty()) {
            return;
        }

        try {
            String localFilename = filename;
            if (localFilename.startsWith("/uploads/products/")) {
                localFilename = localFilename.substring("/uploads/products/".length());
            }
            Path filePath = Paths.get(uploadDir, localFilename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("Failed to delete image file: " + filename);
        }
    }

    public String resolvePublicUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return "https://placehold.co/400x500/1a1a2e/c9a84c?text=No+Image";
        }

        String value = imageUrl.trim();
        if (value.startsWith("http://") || value.startsWith("https://") || value.startsWith("data:") || value.startsWith("/")) {
            return value;
        }

        return "/uploads/products/" + value;
    }

    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0 && lastDot < filename.length() - 1) {
            return filename.substring(lastDot + 1);
        }
        return "";
    }
}
