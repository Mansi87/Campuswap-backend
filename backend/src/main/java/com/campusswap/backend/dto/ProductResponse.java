package com.campusswap.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class ProductResponse {
    private String id;
    private String title;
    private String description;
    private String category;
    private String condition;
    private BigDecimal originalPrice;
    private BigDecimal sellingPrice;
    private BigDecimal aiSuggestedPrice;
    private Integer ageMonths;
    private Map<String, Object> attributes;
    private String[] images;
    private Boolean isSold;
    private LocalDateTime createdAt;

    // Seller info
    private String sellerId;
    private String sellerName;
    private Double sellerRating;
}
