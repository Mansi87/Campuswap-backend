package com.campusswap.backend.service;

import com.campusswap.backend.model.ChatMessage;
import com.campusswap.backend.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;

    // Save a new message
    public ChatMessage saveMessage(UUID senderId, UUID receiverId,
                                   UUID productId, String content) {
        ChatMessage message = new ChatMessage();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setProductId(productId);
        message.setContent(content);
        message.setSentAt(LocalDateTime.now());
        message.setIsRead(false);
        return chatMessageRepository.save(message);
    }

    // Get conversation history
    public List<ChatMessage> getConversation(UUID productId,
                                             UUID userId1, UUID userId2) {
        return chatMessageRepository.findConversation(productId, userId1, userId2);
    }

    // Get all conversations for a user
    public List<ChatMessage> getAllUserMessages(UUID userId) {
        return chatMessageRepository.findAllUserMessages(userId);
    }

    // Get unread message count
    public Long getUnreadCount(UUID userId) {
        return chatMessageRepository.countUnreadMessages(userId);
    }

    // Mark messages as read when user opens conversation
    @Transactional
    public void markMessagesAsRead(UUID productId, UUID senderId, UUID receiverId) {
        List<ChatMessage> unreadMessages = chatMessageRepository
                .findUnreadMessages(productId, senderId, receiverId);
        unreadMessages.forEach(msg -> msg.setIsRead(true));
        chatMessageRepository.saveAll(unreadMessages);
    }
}
