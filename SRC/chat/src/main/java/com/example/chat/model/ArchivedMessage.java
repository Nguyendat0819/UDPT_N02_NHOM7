package com.example.chat.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

// Collection này chứa dữ liệu cũ > 7 ngày
@Document(collection = "messages_archive") 
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArchivedMessage {
    @Id
    private String id; // Giữ nguyên ID cũ để truy vết
    private String conversationId;
    private UUID senderId;
    private UUID recipientId;
    private String content;
    private LocalDateTime createdAt;
}