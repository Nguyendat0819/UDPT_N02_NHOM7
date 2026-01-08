package com.example.chat.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "messages") // Khớp với collection bạn tạo trong Compass
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    @Id
    private String id; // ID tự sinh của MongoDB (ObjectId)
    
    private String conversationId; // ID của phòng chat
    
    private UUID senderId; // UUID của người gửi (khớp với ID từ PostgreSQL)
    
    private String content; // Nội dung tin nhắn
    
    private LocalDateTime createdAt = LocalDateTime.now();
}