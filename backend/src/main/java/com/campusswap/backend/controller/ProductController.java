package com.campusswap.backend.controller;

import com.campusswap.backend.dto.ProductRequest;
import com.campusswap.backend.dto.ProductResponse;
import com.campusswap.backend.service.MLService;
import com.campusswap.backend.service.ProductService;
import org.springframework.security.core.Authentication;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final MLService mlService;

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductRequest request,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            ProductResponse response = productService.createProduct(request, userEmail);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable String id) {
        try {
            ProductResponse response = productService.getProductById(UUID.fromString(id));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // SWIPE FEED
    @GetMapping("/feed")
    public ResponseEntity<List<ProductResponse>> getFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        try {
            List<ProductResponse> products = productService
                    .getFeed(authentication.getName(), page, size);
            return ResponseEntity.ok(products);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // MY LISTINGs
    @GetMapping("/my-listings")
    public ResponseEntity<List<ProductResponse>> getMyListings(
            Authentication authentication) {
        try {
            List<ProductResponse> products = productService
                    .getMyListings(authentication.getName());
            return ResponseEntity.ok(products);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // MARK AS SOLD
    @PutMapping("/{id}/sold")
    public ResponseEntity<?> markAsSold(
            @PathVariable String id,
            Authentication authentication) {
        try {
            ProductResponse response = productService
                    .markAsSold(UUID.fromString(id), authentication.getName());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    //DELETE PRODUCT
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(
            @PathVariable String id,
            Authentication authentication) {
        try {
            productService.deleteProduct(
                    UUID.fromString(id), authentication.getName());
            return ResponseEntity.ok(Map.of("message", "Product deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    //Search
    @GetMapping("/search")
    public ResponseEntity<?> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String condition,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            Authentication authentication) {
        try {
            List<ProductResponse> products = productService.searchProducts(
                    authentication.getName(),
                    keyword,
                    category,
                    condition,
                    minPrice,
                    maxPrice
            );
            return ResponseEntity.ok(products);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/recommendations")
    public ResponseEntity<?> getRecommendations(
            @PathVariable String id) {
        try {
            List<Map<String, Object>> recommendations =
                    mlService.getRecommendations(id);
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/recommendations/category")
    public ResponseEntity<?> getRecommendationsByCategory(
            @RequestParam String category,
            @RequestParam(defaultValue = "5") int topN) {
        try {
            List<Map<String, Object>> recommendations =
                    mlService.getRecommendationsByCategory(category, topN);
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(
            @PathVariable String id,
            @Valid @RequestBody ProductRequest request,
            Authentication authentication) {
        try {
            ProductResponse response = productService.updateProduct(
                    UUID.fromString(id), request, authentication.getName());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
