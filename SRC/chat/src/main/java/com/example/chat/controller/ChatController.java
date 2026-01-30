package com.example.chat.controller;

import com.example.chat.config.LocalDateTimeTypeAdapter;
import com.example.chat.model.ChatRequest;
import com.example.chat.model.Message;
import com.example.chat.repository.MessageRepository;
import com.example.chat.service.UserService;
import com.example.chat.service.ChatService; 
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.UUID;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private ChatService chatService;

    @Autowired
    private MessageRepository messageRepository; 

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
            .create();

    // --- 1. WebSocket: Xử lý Gửi tin nhắn ---
    @MessageMapping("/chat")
    public void processMessage(@Payload ChatRequest request, Principal principal) {
        try {
            String senderEmail = principal.getName();
            UUID senderUuid = userService.findIdByEmail(senderEmail);
            String recipientEmail = request.getRecipientEmail();
            UUID recipientUuid = userService.findIdByEmail(recipientEmail);

            Message message = new Message();
            message.setId(UUID.randomUUID().toString());
            message.setSenderId(senderUuid);
            message.setRecipientId(recipientUuid);
            message.setContent(request.getContent());
            message.setCreatedAt(LocalDateTime.now());

            String conversationId;
            if (senderUuid.compareTo(recipientUuid) < 0) {
                conversationId = senderUuid.toString() + "_" + recipientUuid.toString();
            } else {
                conversationId = recipientUuid.toString() + "_" + senderUuid.toString();
            }
            message.setConversationId(conversationId);

            // 1. Lưu DB
            messageRepository.save(message);

            // 2. Gửi Socket
            messagingTemplate.convertAndSendToUser(recipientEmail, "/queue/messages", message);
            messagingTemplate.convertAndSendToUser(senderEmail, "/queue/messages", message);

            // 3. Cache Redis
            String jsonMessage = gson.toJson(message);
            redisTemplate.opsForList().rightPush("chat_buffer", jsonMessage);

        } catch (Exception e) {
            System.err.println(" Lỗi Chat Controller: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // --- 2. API: Lấy tin nhắn mới (Recent) ---
    // Gọi: /messages/sender@gmail.com/recipient@gmail.com
    @GetMapping("/messages/{sender}/{recipient}")
    public ResponseEntity<?> getRecent(@PathVariable String sender, @PathVariable String recipient) {
        return ResponseEntity.ok(chatService.findRecentMessages(sender, recipient));
    }

    // --- 3. API: Lấy tin nhắn cũ (Archive - Có phân trang) ---
    // Gọi: /api/messages/archive?senderId=...&recipientId=...&page=0
    @GetMapping("/api/messages/archive") // Đã thêm /api vào trước cho chuẩn
    public ResponseEntity<?> getArchive(
            @RequestParam String senderId, 
            @RequestParam String recipientId, 
            @RequestParam(defaultValue = "0") int page) {
        
        // Gọi xuống Service để xử lý gọn gàng
        return ResponseEntity.ok(chatService.findArchivedMessages(senderId, recipientId, page));
    }
}