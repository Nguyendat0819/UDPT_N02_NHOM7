package com.example.chat.scheduler;

import com.example.chat.model.ArchivedMessage;
import com.example.chat.model.Message;
import com.example.chat.repository.ArchivedMessageRepository;
import com.example.chat.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class ArchiveJob {

    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private ArchivedMessageRepository archivedMessageRepository;

    //  Chạy mỗi 30 phút (30 * 60 * 1000 = 1800000 ms)
    @Scheduled(fixedRate = 1800000) 
    // @Scheduled(fixedRate = 5000)
    @Transactional 
    public void moveOldMessagesToArchive() {
        
        // THƯỚC ĐO: Chỉ lấy những tin cũ hơn 7 ngày so với bây giờ
        // Ví dụ: Bây giờ là 25/01 10:30 -> Threshold là 18/01 10:30
        LocalDateTime threshold = LocalDateTime.now().minusDays(7);
        // LocalDateTime threshold = LocalDateTime.now();
        // Robot tìm tin nhắn
        List<Message> oldMessages = messageRepository.findByCreatedAtBefore(threshold);

        if (oldMessages.isEmpty()) {
            return; // Không có tin nào quá hạn 7 ngày -> Đi ngủ tiếp
        }

        // Chuyển sang Archive
        List<ArchivedMessage> archives = new ArrayList<>();
        for (Message msg : oldMessages) {
             // ... copy dữ liệu ...
             archives.add(convertToArchive(msg));
        }
        archivedMessageRepository.saveAll(archives);

        // Xóa khỏi Recent (Để bảng Recent luôn gọn nhẹ, chỉ chứa 7 ngày)
        messageRepository.deleteAll(oldMessages);

        System.out.println(" Đã chuyển " + oldMessages.size() + " tin nhắn (cũ hơn 7 ngày) sang kho lưu trữ.");
    }
    
    // Hàm phụ convert cho gọn code
    private ArchivedMessage convertToArchive(Message msg) {
        return new ArchivedMessage(msg.getId(), msg.getConversationId(), msg.getSenderId(), 
                                   msg.getRecipientId(), msg.getContent(), msg.getCreatedAt());
    }
}