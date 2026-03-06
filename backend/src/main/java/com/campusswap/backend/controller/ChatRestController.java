package com.campusswap.backend.controller;

import com.campusswap.backend.model.ChatMessage;
import com.campusswap.backend.security.JwtUtil;
import com.campusswap.backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatService chatService;
    private final JwtUtil jwtUtil;

    // GET /api/chat/history/{productId}/{otherUserId}
    // Get conversation history for a product between two users
    @GetMapping("/history/{productId}/{otherUserId}")
    public ResponseEntity<List<ChatMessage>> getHistory(
            @PathVariable UUID productId,
            @PathVariable UUID otherUserId,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        UUID currentUserId = jwtUtil.getUserIdFromToken(token);

        List<ChatMessage> messages = chatService.getConversation(
                productId, currentUserId, otherUserId
        );
        return ResponseEntity.ok(messages);
    }

    // GET /api/chat/conversations
    // Get all conversations for logged-in user
    @GetMapping("/conversations")
    public ResponseEntity<List<ChatMessage>> getConversations(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        UUID currentUserId = jwtUtil.getUserIdFromToken(token);

        List<ChatMessage> messages = chatService.getAllUserMessages(currentUserId);
        return ResponseEntity.ok(messages);
    }

    // GET /api/chat/unread-count
    // Get unread message count for logged-in user
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        UUID currentUserId = jwtUtil.getUserIdFromToken(token);

        Long count = chatService.getUnreadCount(currentUserId);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    // PUT /api/chat/mark-read/{productId}/{otherUserId}
    // Mark messages as read when user opens a conversation
    @PutMapping("/mark-read/{productId}/{otherUserId}")
    public ResponseEntity<Map<String, String>> markAsRead(
            @PathVariable UUID productId,
            @PathVariable UUID otherUserId,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        UUID currentUserId = jwtUtil.getUserIdFromToken(token);

        // Mark messages from otherUser to currentUser as read
        chatService.markMessagesAsRead(productId, otherUserId, currentUserId);
        return ResponseEntity.ok(Map.of("status", "messages marked as read"));
    }
}
