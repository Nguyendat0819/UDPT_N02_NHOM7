package com.example.chat.service;

import com.example.chat.model.ChatRequest;
import com.example.chat.model.Message;
import com.example.chat.model.User;
import com.example.chat.repository.FriendshipRepository;
import com.example.chat.repository.MessageRepository;
import com.example.chat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class ChatService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private FriendshipRepository friendshipRepository;
    /**
     * Xử lý logic lưu tin nhắn mới
     */
    public Message saveMessage(String senderEmail, ChatRequest request) {
        User sender = userRepository.findByEmail(senderEmail);
        User recipient = userRepository.findByEmail(request.getRecipientEmail());

        if (sender != null && recipient != null) {

            boolean isBlocked = friendshipRepository.isSenderBlockedByRecipient(recipient.getId(), sender.getId());
            
            if (isBlocked) {
                // Tùy chọn: Có thể return null để không lưu tin nhắn
                // Hoặc ném lỗi để Frontend biết
                System.out.println("Tin nhắn bị chặn do User " + recipient.getUsername() + " đã block " + sender.getUsername());
                return null; 
            }
            // 1. Tạo Conversation ID chuẩn
            String conversationId = generateConversationId(sender.getId(), recipient.getId());

            // 2. Tạo đối tượng Message
            Message message = new Message();
            message.setConversationId(conversationId);
            message.setSenderId(sender.getId());
            message.setContent(request.getContent());
            message.setCreatedAt(LocalDateTime.now());

            // 3. Lưu xuống MongoDB và trả về kết quả
            return messageRepository.save(message);
        }
        return null; // Hoặc ném Exception tùy bạn
    }

    /**
     * Xử lý logic lấy lịch sử chat
     */
    public List<Message> getChatHistory(String senderEmail, String recipientEmail) {
        User sender = userRepository.findByEmail(senderEmail);
        User recipient = userRepository.findByEmail(recipientEmail);

        if (sender != null && recipient != null) {
            String conversationId = generateConversationId(sender.getId(), recipient.getId());
            // Gọi hàm repository bạn vừa viết
            return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        }
        return Collections.emptyList();
    }

    // Logic private: Chỉ service này cần biết cách tạo ID
    private String generateConversationId(UUID userId1, UUID userId2) {
        if (userId1.compareTo(userId2) < 0) {
            return userId1 + "_" + userId2;
        } else {
            return userId2 + "_" + userId1;
        }
    }
}