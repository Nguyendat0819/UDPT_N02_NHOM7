package com.example.chat.repository;

import com.example.chat.model.Message;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<Message, String> {
    // Lấy lịch sử tin nhắn của một phòng chat, sắp xếp theo thời gian
    List<Message> findByConversationIdOrderByCreatedAtAsc(String conversationId);
}