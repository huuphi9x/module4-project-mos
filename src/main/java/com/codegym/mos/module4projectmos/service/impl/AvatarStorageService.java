package com.codegym.mos.module4projectmos.service.impl;

import com.codegym.mos.module4projectmos.exception.FileNotFoundException;
import com.codegym.mos.module4projectmos.exception.FileStorageException;
import com.codegym.mos.module4projectmos.model.entity.User;
import com.codegym.mos.module4projectmos.property.AvatarStorageProperties;
import com.codegym.mos.module4projectmos.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class AvatarStorageService implements StorageService<User> {
    private final Path avatarStorageLocation;

    @Autowired
    public AvatarStorageService(AvatarStorageProperties avatarStorageLocation) {
        this.avatarStorageLocation = Paths.get(avatarStorageLocation.getUploadDir())
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.avatarStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    @Override
    public String storeFile(MultipartFile file, User user) {
        String originalFileName = file.getOriginalFilename();
        // Get file extension
        String extension = originalFileName != null ? originalFileName.substring(originalFileName.lastIndexOf(".") + 1) : "";
        String avatarUrl = user.getAvatarUrl();

        // check if new image ext is different from old file ext
        if (avatarUrl != null && !avatarUrl.equals("")) {
            String oldExtension = avatarUrl.substring(avatarUrl.lastIndexOf(".") + 1);
            if (!oldExtension.equals(extension)) {
                String oldFileName = user.getId().toString().concat(" - ").concat(user.getUsername()).concat(".").concat(oldExtension);
                deleteFile(oldFileName);
            }
        }

        // Normalize file name
        String fileName = StringUtils.cleanPath(user.getId().toString()).concat(" - ").concat(user.getUsername()).concat(".").concat(extension);
        try {
            // Check if the file's name contains invalid characters
            if (fileName.contains("..")) {
                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
            }
            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = this.avatarStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    @Override
    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.avatarStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new FileNotFoundException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new FileNotFoundException("File not found " + fileName, ex);
        }
    }

    @Override
    public Boolean deleteFile(String fileName) {
        Path filePath = this.avatarStorageLocation.resolve(fileName).normalize();
        File file = filePath.toFile();
        return file.delete();
    }
}
