package com.campusswap.backend.controller;


import com.campusswap.backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // Create Razorpay order
    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        try {
            Map<String, Object> order = paymentService.createOrder(
                    UUID.fromString(request.get("noteId")),
                    authentication.getName());
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Verify payment and unlock download
    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        boolean verified = paymentService.verifyAndSavePurchase(
                request.get("razorpayOrderId"),
                request.get("razorpayPaymentId"),
                request.get("razorpaySignature"),
                UUID.fromString(request.get("noteId")),
                authentication.getName());

        if (verified) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Payment verified!"));
        } else {
            return ResponseEntity.status(400).body(Map.of("success", false,
                    "message", "Payment verification failed"));
        }
    }
}
