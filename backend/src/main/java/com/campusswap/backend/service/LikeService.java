package com.campusswap.backend.service;

import com.campusswap.backend.dto.ProductResponse;
import com.campusswap.backend.model.Like;
import com.campusswap.backend.model.Product;
import com.campusswap.backend.model.User;
import com.campusswap.backend.repository.LikeRepository;
import com.campusswap.backend.repository.ProductRepository;
import com.campusswap.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.campusswap.backend.model.Notification;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    // Like a product (Swipe Right)
    @Transactional
    public String likeProduct(UUID productId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (likeRepository.existsByUserAndProduct(user, product)) {
            throw new RuntimeException("You already liked this product");
        }

        // Create like
        Like like = new Like();
        like.setUser(user);
        like.setProduct(product);
        likeRepository.save(like);

        // Notify seller (only if liker isn't the seller themselves)
        if (!product.getSeller().getId().equals(user.getId())) {
            notificationService.createNotification(
                    product.getSeller(),
                    "LIKE",
                    user.getFullName() + " liked your \"" + product.getTitle() + "\"",
                    product.getId().toString()
            );
        }

        return "Product liked successfully";
    }

    @Transactional
    public String unlikeProduct(UUID productId, String userEmail) {
        // Get user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Delete like
        likeRepository.deleteByUserAndProduct(user, product);

        return "Product unliked successfully";
    }

    // Get all liked products for a user
    public List<ProductResponse> getMyLikes(String userEmail) {
        // Get user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get all likes
        List<Like> likes = likeRepository.findByUserOrderByCreatedAtDesc(user);

        // Convert to ProductResponse
        return likes.stream()
                .map(like -> mapToProductResponse(like.getProduct()))
                .collect(Collectors.toList());
    }

    public boolean isProductLiked(UUID productId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        return likeRepository.existsByUserAndProduct(user, product);
    }

    public long getLikeCount(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        return likeRepository.countByProduct(product);
    }

    private ProductResponse mapToProductResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId().toString());
        response.setTitle(product.getTitle());
        response.setDescription(product.getDescription());
        response.setCategory(product.getCategory());
        response.setCondition(product.getCondition());
        response.setOriginalPrice(product.getOriginalPrice());
        response.setSellingPrice(product.getSellingPrice());
        response.setAiSuggestedPrice(product.getAiSuggestedPrice());
        response.setAgeMonths(product.getAgeMonths());
        response.setAttributes(product.getAttributes());
        response.setImages(product.getImages());
        response.setIsSold(product.getIsSold());
        response.setCreatedAt(product.getCreatedAt());
        response.setSellerId(product.getSeller().getId().toString());
        response.setSellerName(product.getSeller().getFullName());
        response.setSellerRating(product.getSeller().getRating());
        return response;
    }
}
