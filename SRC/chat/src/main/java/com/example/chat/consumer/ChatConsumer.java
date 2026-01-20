package com.example.chat.consumer;

import com.example.chat.model.Message;
import com.example.chat.repository.MessageRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ChatConsumer {
    @Autowired
    private MessageRepository messageRepository;

    // Worker lắng nghe hàng đợi và lưu vào MongoDB
    @RabbitListener(queues = "chat_queue")
    public void saveMessage(Message message) {
        System.out.println("✅ [Worker] Nhận tin từ RabbitMQ: " + message.getContent());
        messageRepository.save(message);
    }
}