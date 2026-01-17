package com.example.chat.model;

import lombok.Data;

@Data
public class ChatRequest {
    private String recipientEmail; // Email người nhận
    private String content;        // Nội dung tin nhắn
}