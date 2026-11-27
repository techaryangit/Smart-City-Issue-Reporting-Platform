package com.smartcity.issuereporting.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

/** Stores complaint photographs on the local server disk (initial implementation). */
@Service
public class FileStorageService {

    private static final Map<String, String> ALLOWED = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp");

    private final Path root;

    public FileStorageService(@Value("${app.upload-dir}") String uploadDir) throws IOException {
        this.root = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(root);
    }

    public Path getRoot() { return root; }

    /** Saves the image under a random name and returns that name, or null if no file was sent. */
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        String ext = ALLOWED.get(file.getContentType());
        if (ext == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only JPG, PNG or WEBP images are allowed");
        }
        String name = UUID.randomUUID() + ext;
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, root.resolve(name), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not store the image");
        }
        return name;
    }
}
