package com.campusswap.backend.service;

import com.campusswap.backend.dto.ProductRequest;
import com.campusswap.backend.dto.ProductResponse;
import com.campusswap.backend.model.Product;
import com.campusswap.backend.model.User;
import com.campusswap.backend.repository.ProductRepository;
import com.campusswap.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ProductResponse createProduct(ProductRequest request, String userEmail) {
        // Get seller
        User seller = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create product
        Product product = new Product();
        product.setSeller(seller);
        product.setTitle(request.getTitle());
        product.setDescription(request.getDescription());
        product.setCategory(request.getCategory());
        product.setCondition(request.getCondition());
        product.setOriginalPrice(request.getOriginalPrice());
        product.setSellingPrice(request.getSellingPrice());
        product.setAgeMonths(request.getAgeMonths());
        product.setAttributes(request.getAttributes());
        product.setImages(request.getImages());
        product.setCollege(seller.getCollege());
        product.setAiSuggestedPrice(request.getSellingPrice());

        product = productRepository.save(product);

        return mapToResponse(product);
    }
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ProductResponse getProductById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return mapToResponse(product);
    }

    private ProductResponse mapToResponse(Product product) {
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
