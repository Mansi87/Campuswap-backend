package com.campusswap.backend.controller;

import com.campusswap.backend.dto.FileUploadResponse;
import com.campusswap.backend.model.User;
import com.campusswap.backend.repository.UserRepository;
import com.campusswap.backend.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    private final FileUploadService fileUploadService;
    private final UserRepository userRepository;

    // Upload single product image
    @PostMapping("/image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            FileUploadResponse response = fileUploadService.uploadImage(file, "products");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Upload multiple product images (max 4)
    @PostMapping("/images")
    public ResponseEntity<?> uploadImages(@RequestParam("files") List<MultipartFile> files) {
        try {
            List<FileUploadResponse> responses = fileUploadService.uploadImages(files, "products");

            // Return just URLs in simple format
            Map<String, Object> result = new HashMap<>();
            result.put("urls", responses.stream()
                    .map(FileUploadResponse::getUrl)
                    .collect(Collectors.toList()));
            result.put("count", responses.size());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Upload profile picture
    @PostMapping("/profile-picture")
    public ResponseEntity<?> uploadProfilePicture(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        try {
            // Get user
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Upload image
            FileUploadResponse response = fileUploadService.uploadProfilePicture(
                    file,
                    user.getId().toString()
            );

            // Update user profile picture
            user.setProfilePicUrl(response.getUrl());
            userRepository.save(user);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
