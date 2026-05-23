package com.campusswap.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MLService {

    @Value("${ml.service.url}")
    private String mlServiceUrl;

    private final RestTemplate restTemplate;

    // ── PRICE PREDICTION ──────────────────────────────────────
    public BigDecimal predictPrice(String category, String condition,
                                   BigDecimal originalPrice, Integer ageMonths) {
        try {
            // Build request body
            Map<String, Object> request = new HashMap<>();
            request.put("category", category);
            request.put("condition", condition);
            request.put("original_price", originalPrice);
            request.put("age_months", ageMonths != null ? ageMonths : 0);

            // Call Flask
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity =
                    new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    mlServiceUrl + "/predict-price",
                    entity,
                    Map.class
            );
            // Extract suggested price
            if (response.getStatusCode() == HttpStatus.OK
                    && response.getBody() != null) {
                Object price = response.getBody().get("suggested_price");
                return new BigDecimal(price.toString());
            }
        } catch (Exception e) {
            // If ML service is down → don't crash
            // Just return null → fallback to selling price
            System.out.println("ML Service unavailable: " + e.getMessage());
        }
        return null;
    }

    // ── PRODUCT RECOMMENDATION ────────────────────────────────
    public List<Map<String, Object>> getRecommendations(String productId) {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    mlServiceUrl + "/recommend/" + productId,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK
                    && response.getBody() != null) {
                return (List<Map<String, Object>>)
                        response.getBody().get("recommendations");
            }

        } catch (Exception e) {
            System.out.println("ML Service unavailable: " + e.getMessage());
        }

        return List.of();
    }

    // ── RECOMMEND BY CATEGORY ─────────────────────────────────
    public List<Map<String, Object>> getRecommendationsByCategory(
            String category, int topN) {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    mlServiceUrl + "/recommend-by-category?category="
                            + category + "&top_n=" + topN,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK
                    && response.getBody() != null) {
                return (List<Map<String, Object>>)
                        response.getBody().get("products");
            }

        } catch (Exception e) {
            System.out.println("ML Service unavailable: " + e.getMessage());
        }

        return List.of();
    }
}
