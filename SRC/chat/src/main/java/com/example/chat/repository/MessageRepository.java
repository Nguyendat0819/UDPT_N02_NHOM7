package com.example.chat.repository;

import com.example.chat.model.Message;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface MessageRepository extends MongoRepository<Message, String> {
    // Tìm tin nhắn theo phòng chat
    List<Message> findByConversationIdOrderByCreatedAtAsc(String conversationId);
    
    // HÀM QUAN TRỌNG: Tìm tin nhắn cũ hơn thời gian quy định
    List<Message> findByCreatedAtBefore(LocalDateTime threshold);
}