package com.campusswap.backend.repository;

import com.campusswap.backend.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    // Get full conversation between two users about a specific product
    @Query("SELECT m FROM ChatMessage m WHERE m.productId = :productId " +
            "AND ((m.senderId = :userId1 AND m.receiverId = :userId2) " +
            "OR (m.senderId = :userId2 AND m.receiverId = :userId1)) " +
            "ORDER BY m.sentAt ASC")
    List<ChatMessage> findConversation(
            @Param("productId") UUID productId,
            @Param("userId1") UUID userId1,
            @Param("userId2") UUID userId2
    );

    // Get all conversations for a user (latest message per conversation)
    @Query("SELECT m FROM ChatMessage m WHERE m.senderId = :userId " +
            "OR m.receiverId = :userId ORDER BY m.sentAt DESC")
    List<ChatMessage> findAllUserMessages(@Param("userId") UUID userId);

    // Count unread messages for a user
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.receiverId = :userId " +
            "AND m.isRead = false")
    Long countUnreadMessages(@Param("userId") UUID userId);

    // Mark messages as read
    @Query("SELECT m FROM ChatMessage m WHERE m.productId = :productId " +
            "AND m.senderId = :senderId AND m.receiverId = :receiverId " +
            "AND m.isRead = false")
    List<ChatMessage> findUnreadMessages(
            @Param("productId") UUID productId,
            @Param("senderId") UUID senderId,
            @Param("receiverId") UUID receiverId
    );
}
