package com.campusswap.backend.service;

import com.campusswap.backend.model.*;
import com.campusswap.backend.repository.*;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final PurchaseRepository purchaseRepository;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    // Step 1 — Create Razorpay order
    public Map<String, Object> createOrder(UUID noteId, String userEmail) throws RazorpayException {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found"));
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (note.isFree()) throw new RuntimeException("This note is free, no payment needed");
        if (note.getSeller().getId().equals(user.getId()))
            throw new RuntimeException("You cannot buy your own note");
        if (purchaseRepository.existsByBuyerAndNote(user, note))
            throw new RuntimeException("Already purchased");

        // Amount in paise (₹1 = 100 paise)
        int amountInPaise = note.getPrice()
                .multiply(BigDecimal.valueOf(100))
                .intValue();

        RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "note_" + noteId);

        Order order = client.orders.create(orderRequest);

        return Map.of(
                "orderId", order.get("id"),
                "amount", amountInPaise,
                "currency", "INR",
                "keyId", razorpayKeyId,
                "noteTitle", note.getTitle()
        );
    }

    // Step 2 — Verify payment signature (the security step!)
    public boolean verifyAndSavePurchase(String razorpayOrderId,
                                         String razorpayPaymentId,
                                         String razorpaySignature,
                                         UUID noteId,
                                         String userEmail) {
        try {
            // Recreate the signature on our server
            String payload = razorpayOrderId + "|" + razorpayPaymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    razorpayKeySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String generatedSignature = HexFormat.of().formatHex(hash);

            // Compare with signature from client
            if (!generatedSignature.equals(razorpaySignature)) {
                return false; // Signature mismatch — payment is FAKE
            }

            // Signature matches — payment is REAL, save purchase
            Note note = noteRepository.findById(noteId)
                    .orElseThrow(() -> new RuntimeException("Note not found"));
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Purchase purchase = new Purchase();
            purchase.setBuyer(user);
            purchase.setNote(note);
            purchase.setRazorpayOrderId(razorpayOrderId);
            purchase.setRazorpayPaymentId(razorpayPaymentId);
            purchaseRepository.save(purchase);

            return true;

        } catch (Exception e) {
            return false;
        }
    }
}
