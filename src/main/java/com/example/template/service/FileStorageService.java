package com.example.template.service;

import com.example.template.exception.BusinessException;
import com.example.template.exception.ResourceNotFoundException;
import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@ApplicationScoped
@Slf4j
public class FileStorageService {

    @ConfigProperty(name = "app.file.upload-dir", defaultValue = "uploads")
    String uploadDir;

    private Path uploadPath;

    void init(@Observes @Priority(1) StartupEvent ev) {
        uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadPath);
            log.info("Upload directory siap: {}", uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("Gagal membuat direktori upload: " + uploadPath, e);
        }
    }

    /**
     * Simpan file ke disk. Return nama file unik (UUID + ekstensi asli).
     *
     * @param originalFilename nama file asli dari client
     * @param inputStream      stream konten file
     * @return nama file yang disimpan di server
     */
    public String storeFile(String originalFilename, InputStream inputStream) {
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new BusinessException("Nama file tidak boleh kosong");
        }

        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }

        String filename = UUID.randomUUID().toString() + extension;

        Path targetPath = uploadPath.resolve(filename).normalize();
        if (!targetPath.startsWith(uploadPath)) {
            throw new BusinessException("Nama file tidak valid");
        }

        try {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("File tersimpan: {}", filename);
            return filename;
        } catch (IOException e) {
            throw new RuntimeException("Gagal menyimpan file: " + e.getMessage(), e);
        }
    }

    /**
     * Load file sebagai Path untuk di-stream ke client.
     */
    public Path loadFileAsPath(String filename) {
        Path filePath = uploadPath.resolve(filename).normalize();

        if (!filePath.startsWith(uploadPath)) {
            throw new ResourceNotFoundException("File tidak ditemukan: " + filename);
        }
        if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
            throw new ResourceNotFoundException("File tidak ditemukan atau tidak bisa dibaca: " + filename);
        }

        return filePath;
    }
}
