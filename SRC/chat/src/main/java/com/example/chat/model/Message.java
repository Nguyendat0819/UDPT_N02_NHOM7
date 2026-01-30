package com.example.chat.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

// Đổi tên collection thành messages_recent cho rõ ràng
@Document(collection = "messages_recent") 
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    @Id
    private String id;
    private String conversationId;
    private UUID senderId;
    private UUID recipientId;
    private String content;
    private LocalDateTime createdAt = LocalDateTime.now();
}