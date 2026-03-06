package com.campusswap.backend.controller;

import com.campusswap.backend.dto.ProductResponse;
import com.campusswap.backend.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    // Like a product (Swipe Right)
    @PostMapping("/{productId}")
    public ResponseEntity<Map<String, String>> likeProduct(
            @PathVariable String productId,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            String message = likeService.likeProduct(UUID.fromString(productId), userEmail);

            Map<String, String> response = new HashMap<>();
            response.put("message", message);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Unlike a product
    @DeleteMapping("/{productId}")
    public ResponseEntity<Map<String, String>> unlikeProduct(
            @PathVariable String productId,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            String message = likeService.unlikeProduct(UUID.fromString(productId), userEmail);

            Map<String, String> response = new HashMap<>();
            response.put("message", message);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/my-likes")
    public ResponseEntity<List<ProductResponse>> getMyLikes(Authentication authentication) {
        String userEmail = authentication.getName();
        List<ProductResponse> likes = likeService.getMyLikes(userEmail);
        return ResponseEntity.ok(likes);
    }

    //Check if product is liked
    @GetMapping("/check/{productId}")
    public ResponseEntity<Map<String, Boolean>> checkIfLiked(
            @PathVariable String productId,
            Authentication authentication) {
        String userEmail = authentication.getName();
        boolean isLiked = likeService.isProductLiked(UUID.fromString(productId), userEmail);

        Map<String, Boolean> response = new HashMap<>();
        response.put("isLiked", isLiked);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/count/{productId}")
    public ResponseEntity<Map<String, Long>> getLikeCount(@PathVariable String productId) {
        long count = likeService.getLikeCount(UUID.fromString(productId));

        Map<String, Long> response = new HashMap<>();
        response.put("likeCount", count);
        return ResponseEntity.ok(response);
    }
}
