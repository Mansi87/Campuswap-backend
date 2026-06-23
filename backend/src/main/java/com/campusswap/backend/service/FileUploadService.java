package com.campusswap.backend.service;

import com.campusswap.backend.dto.FileUploadResponse;
import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final Cloudinary cloudinary;

    // Allowed image types
    private static final List<String> ALLOWED_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/webp"
    );

    // Max file size: 10MB
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    // Upload single image
    public FileUploadResponse uploadImage(MultipartFile file, String folder) throws IOException {
        // Validate file
        validateFile(file);

        // Upload to Cloudinary with transformations
        Map<String, Object> uploadParams = ObjectUtils.asMap(
                "folder", folder,
                "resource_type", "image",
                "transformation", new Transformation()
                        .width(1920)
                        .height(1080)
                        .crop("limit")
                        .quality("auto:good")
                        .fetchFormat("auto")
        );

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);

        // Build response
        return new FileUploadResponse(
                (String) uploadResult.get("secure_url"),
                (String) uploadResult.get("public_id"),
                (String) uploadResult.get("format"),
                ((Number) uploadResult.get("bytes")).longValue()
        );
    }

    public String uploadPdf(MultipartFile file, String folder) {
        try {
            Map<String, Object> options = new HashMap<>();
            options.put("folder", folder);
            options.put("resource_type", "raw"); // PDF needs "raw" not "image"
            options.put("format", "pdf");

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
            return (String) uploadResult.get("secure_url");
        } catch (Exception e) {
            throw new RuntimeException("PDF upload failed: " + e.getMessage());
        }
    }

    // Upload multiple images
    public List<FileUploadResponse> uploadImages(List<MultipartFile> files, String folder) throws IOException {
        // Max 4 images
        if (files.size() > 4) {
            throw new RuntimeException("Maximum 4 images allowed");
        }

        List<FileUploadResponse> responses = new ArrayList<>();
        for (MultipartFile file : files) {
            responses.add(uploadImage(file, folder));
        }
        return responses;
    }

    // Upload profile picture
    public FileUploadResponse uploadProfilePicture(MultipartFile file, String userId) throws IOException {
        validateFile(file);

        // Upload with circular crop for profile pictures
        Map<String, Object> uploadParams = ObjectUtils.asMap(
                "folder", "profiles",
                "public_id", userId,  // Overwrite existing profile pic
                "resource_type", "image",
                "transformation", new Transformation()
                        .width(400)
                        .height(400)
                        .crop("fill")
                        .gravity("face")
                        .quality("auto:good")
        );

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);

        return new FileUploadResponse(
                (String) uploadResult.get("secure_url"),
                (String) uploadResult.get("public_id"),
                (String) uploadResult.get("format"),
                ((Number) uploadResult.get("bytes")).longValue()
        );
    }

    // Validate file
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        // Check file type
        String contentType = file.getContentType();
        if (!ALLOWED_TYPES.contains(contentType)) {
            throw new RuntimeException("Invalid file type. Only JPG, PNG, and WEBP are allowed");
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("File size exceeds 10MB limit");
        }
    }
}
