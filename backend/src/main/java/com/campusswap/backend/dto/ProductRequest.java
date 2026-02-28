package com.campusswap.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class ProductRequest {
    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Condition is required")
    private String condition;

    private BigDecimal originalPrice;

    @NotNull(message = "Selling price is required")
    private BigDecimal sellingPrice;

    private Integer ageMonths;

    private Map<String, Object> attributes;  // Flexible fields (JSONB)

    private String[] images;
}
