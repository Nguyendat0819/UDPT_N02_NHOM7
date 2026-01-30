package com.example.chat.service;
import com.example.chat.model.ArchivedMessage;
import com.example.chat.model.Message;
import com.example.chat.model.User;
import com.example.chat.repository.ArchivedMessageRepository;
import com.example.chat.repository.FriendshipRepository;
import com.example.chat.repository.MessageRepository;
import com.example.chat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class ChatService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private FriendshipRepository friendshipRepository;
    @Autowired
    private ArchivedMessageRepository archivedMessageRepository;

    /**
     * ✅ 1. LẤY TIN NHẮN GẦN ĐÂY (Mặc định khi mở chat)
     * Chỉ lấy từ bảng messages (chứa 7 ngày gần nhất)
     */
    public List<Message> findRecentMessages(String senderEmail, String recipientEmail) {
        String conversationId = getConversationIdByEmails(senderEmail, recipientEmail);
        
        // Lấy tất cả tin trong bảng Recent (Vì bảng này ít, chỉ 7 ngày nên findAll ok)
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    /**
     * ✅ 2. LẤY TIN NHẮN CŨ (Archive) - CÓ PHÂN TRANG
     * Hàm này dùng cho tính năng "Cuộn lên xem thêm"
     * @param page: Số trang (0, 1, 2...)
     */
    public List<ArchivedMessage> findArchivedMessages(String senderEmail, String recipientEmail, int page) {
        String conversationId = getConversationIdByEmails(senderEmail, recipientEmail);

        // Quy định: Mỗi lần chỉ lấy 20 tin
        int pageSize = 20;
        Pageable pageable = PageRequest.of(page, pageSize);

        // 🔥 QUAN TRỌNG: Lấy giảm dần (DESC) để lấy những tin "mới nhất trong quá khứ" trước
        Page<ArchivedMessage> resultPage = archivedMessageRepository
                .findByConversationIdOrderByCreatedAtDesc(conversationId, pageable);

        List<ArchivedMessage> messages = new ArrayList<>(resultPage.getContent());

        // Đảo ngược lại danh sách (để hiển thị đúng thứ tự thời gian cũ -> mới trên UI)
        Collections.reverse(messages);

        return messages;
    }

    /**
     * Helper: Lấy ConversationId từ 2 Email
     */
    private String getConversationIdByEmails(String email1, String email2) {
        UUID id1 = userService.findIdByEmail(email1); // Uses Redis Cache
        UUID id2 = userService.findIdByEmail(email2); // Uses Redis Cache
        return generateConversationId(id1, id2);
    }

    // Logic tạo ID hội thoại (Giữ nguyên của bạn)
    private String generateConversationId(UUID userId1, UUID userId2) {
        return (userId1.compareTo(userId2) < 0) 
                ? userId1.toString() + "_" + userId2.toString() 
                : userId2.toString() + "_" + userId1.toString();
    }
    
    // Helper check block (Giữ nguyên của bạn)
    public boolean isUserBlocked(UUID senderId, UUID recipientId) {
        try {
             return friendshipRepository.isSenderBlockedByRecipient(recipientId, senderId);
        } catch (Exception e) {
            return false;
        }
    }
}