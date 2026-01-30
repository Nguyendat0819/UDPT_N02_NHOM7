package com.example.chat.repository;

import com.example.chat.model.ArchivedMessage;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ArchivedMessageRepository extends MongoRepository<ArchivedMessage, String> {
    // Tìm tin nhắn cũ (khi người dùng muốn xem lịch sử xa xưa)
    Page<ArchivedMessage> findByConversationIdOrderByCreatedAtDesc(String conversationId, Pageable pageable);
}