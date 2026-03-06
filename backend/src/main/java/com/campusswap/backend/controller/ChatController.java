package com.campusswap.backend.controller;

import com.campusswap.backend.model.ChatMessage;
import com.campusswap.backend.security.JwtUtil;
import com.campusswap.backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final JwtUtil jwtUtil;

    // Frontend sends to: /app/chat.send
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload Map<String, String> payload) {

        UUID senderId   = UUID.fromString(payload.get("senderId"));
        UUID receiverId = UUID.fromString(payload.get("receiverId"));
        UUID productId  = UUID.fromString(payload.get("productId"));
        String content  = payload.get("content");

        // Save message to database
        ChatMessage saved = chatService.saveMessage(
                senderId, receiverId, productId, content
        );

        // Send to receiver in real-time
        // Receiver subscribes to: /topic/messages/{receiverId}
        messagingTemplate.convertAndSend(
                "/topic/messages/" + receiverId, saved
        );

        // Also send back to sender so they see their own message
        messagingTemplate.convertAndSend(
                "/topic/messages/" + senderId, saved
        );
    }
}
