package com.campusswap.backend.service;

import com.campusswap.backend.dto.ProductRequest;
import com.campusswap.backend.dto.ProductResponse;
import com.campusswap.backend.model.College;
import com.campusswap.backend.model.Product;
import com.campusswap.backend.model.User;
import com.campusswap.backend.repository.ProductRepository;
import com.campusswap.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final MLService mlService;

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
        BigDecimal aiPrice = mlService.predictPrice(
                request.getCategory(),
                request.getCondition(),
                request.getOriginalPrice(),
                request.getAgeMonths()
        );

        // If ML works → use AI price, else fallback to selling price
        product.setAiSuggestedPrice(
                aiPrice != null ? aiPrice : request.getSellingPrice()
        );

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

    // SWIPE FEED — paginated, college scoped
    public List<ProductResponse> getFeed(String userEmail, int page, int size) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        College college = user.getCollege();
        Pageable pageable = PageRequest.of(page, size);

        Page<Product> products = productRepository
                .findFeedProducts(college, user.getId(), pageable);

        // If empty → reset (user has seen everything)
        if (products.isEmpty()) {
            products = productRepository
                    .findFeedProductsReset(college, user.getId(), pageable);
        }

        return products.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // MY LISTINGS — seller's own products
    public List<ProductResponse> getMyListings(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return productRepository
                .findBySellerIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // MARK AS SOLD — only seller can do this
    @Transactional
    public ProductResponse markAsSold(UUID productId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Only seller can mark as sold
        if (!product.getSeller().getId().equals(user.getId())) {
            throw new RuntimeException("You are not authorized to update this product");
        }

        product.setIsSold(true);
        product.setUpdatedAt(LocalDateTime.now());
        product = productRepository.save(product);

        return mapToResponse(product);
    }

    // DELETE PRODUCT — only seller can delete
    @Transactional
    public void deleteProduct(UUID productId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Only seller can delete
        if (!product.getSeller().getId().equals(user.getId())) {
            throw new RuntimeException("You are not authorized to delete this product");
        }

        productRepository.delete(product);
    }

    @Transactional
    public ProductResponse updateProduct(UUID productId, ProductRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!product.getSeller().getId().equals(user.getId())) {
            throw new RuntimeException("Not authorized");
        }

        product.setTitle(request.getTitle());
        product.setDescription(request.getDescription());
        product.setCategory(request.getCategory());
        product.setCondition(request.getCondition());
        product.setOriginalPrice(request.getOriginalPrice());
        product.setSellingPrice(request.getSellingPrice());
        product.setAgeMonths(request.getAgeMonths());
        product.setImages(request.getImages());
        product.setUpdatedAt(LocalDateTime.now());

        product = productRepository.save(product);
        return mapToResponse(product);
    }

    public List<ProductResponse> searchProducts(
            String userEmail,
            String keyword,
            String category,
            String condition,
            BigDecimal minPrice,
            BigDecimal maxPrice) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String cleanKeyword   = (keyword   != null && !keyword.trim().isEmpty())   ? keyword.trim()   : "";
        String cleanCategory  = (category  != null && !category.trim().isEmpty())  ? category.trim()  : "";
        String cleanCondition = (condition != null && !condition.trim().isEmpty()) ? condition.trim() : "";

        List<Product> products = productRepository.searchProducts(
                user.getCollege(),
                user.getId(),
                cleanKeyword,
                cleanCategory,
                cleanCondition,
                minPrice,
                maxPrice
        );

        return products.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
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
        response.setCollegeName(product.getCollege().getName());
        return response;
    }
}
