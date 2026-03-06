package com.campusswap.backend.controller;

import com.campusswap.backend.model.User;
import com.campusswap.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    // Get current user profile
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Build response
            Map<String, Object> profile = new HashMap<>();
            profile.put("id", user.getId().toString());
            profile.put("fullName", user.getFullName());
            profile.put("email", user.getEmail());
            profile.put("phone", user.getPhone());
            profile.put("profilePicUrl", user.getProfilePicUrl());
            profile.put("rating", user.getRating());
            profile.put("isVerified", user.getIsVerified());
            profile.put("collegeName", user.getCollege().getName());
            profile.put("createdAt", user.getCreatedAt());

            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Get user by ID (public view)
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable String userId) {
        try {
            User user = userRepository.findById(java.util.UUID.fromString(userId))
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Public profile (limited info)
            Map<String, Object> profile = new HashMap<>();
            profile.put("id", user.getId().toString());
            profile.put("fullName", user.getFullName());
            profile.put("profilePicUrl", user.getProfilePicUrl());
            profile.put("rating", user.getRating());
            profile.put("collegeName", user.getCollege().getName());

            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "User not found");
            return ResponseEntity.notFound().build();
        }
    }

    // Update profile (name, phone)
    @PutMapping("/profile")
    @Transactional
    public ResponseEntity<?> updateProfile(
            @RequestBody Map<String, String> updates,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Update fields if provided
            if (updates.containsKey("fullName")) {
                user.setFullName(updates.get("fullName"));
            }
            if (updates.containsKey("phone")) {
                user.setPhone(updates.get("phone"));
            }
            user.setUpdatedAt(java.time.LocalDateTime.now());

            user = userRepository.save(user);

            // Return updated profile
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Profile updated successfully");
            response.put("fullName", user.getFullName());
            response.put("phone", user.getPhone());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
