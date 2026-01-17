package com.example.chat.controller;

import com.example.chat.model.ChatRequest;
import com.example.chat.model.Message;
import com.example.chat.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.List;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate; // Chỉ lo việc gửi tin

    @Autowired
    private ChatService chatService; // Chỉ lo việc xử lý dữ liệu

    // 1. NHẬN VÀ GỬI TIN NHẮN (WEBSOCKET)
    @MessageMapping("/chat")
    public void processMessage(@Payload ChatRequest request, Principal principal) {
        // Gọi Service xử lý lưu dữ liệu
        System.out.println("=== LOG CHAT ===");
        System.out.println("Người gửi: " + principal.getName());
        System.out.println("Người nhận (Email): " + request.getRecipientEmail());
        Message savedMessage = chatService.saveMessage(principal.getName(), request);

        if (savedMessage != null) {
            // Controller chỉ làm nhiệm vụ "phát loa" (Routing)
            
            // Gửi cho người nhận
            messagingTemplate.convertAndSendToUser(
                    request.getRecipientEmail(),
                    "/queue/messages",
                    savedMessage
            );

            // Gửi lại cho người gửi (để hiển thị phía họ)
            messagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "/queue/messages",
                    savedMessage
            );
        }
    }

    // 2. LẤY LỊCH SỬ CHAT (HTTP API)
    @GetMapping("/history/{recipientEmail}")
    @ResponseBody
    public List<Message> getChatHistory(@PathVariable String recipientEmail, Principal principal) {
        // Gọi Service lấy dữ liệu và trả về JSON
        return chatService.getChatHistory(principal.getName(), recipientEmail);
    }
}