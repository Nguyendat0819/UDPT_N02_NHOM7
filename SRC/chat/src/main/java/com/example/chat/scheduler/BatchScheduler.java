package com.example.chat.scheduler;

import com.example.chat.config.LocalDateTimeTypeAdapter; // Import Adapter
import com.example.chat.model.Message; // Sửa model thành Message
import com.example.chat.repository.MessageRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class BatchScheduler {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private MessageRepository messageRepository;

    // QUAN TRỌNG: Cấu hình Gson để hiểu LocalDateTime (Giống Controller)
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
            .create();

    // Chạy mỗi 30 giây (30000 ms)
    @Scheduled(fixedRate = 30000)
    public void flushRedisToMongo() {
        
        List<String> jsonMessages = new ArrayList<>();
        
        // 1. Lấy toàn bộ tin nhắn từ Redis (FIFO Queue)
        // Dùng vòng lặp pop liên tục cho đến khi hết
        while (true) {
            // SỬA KEY: Phải khớp với "chat_buffer" bên Controller
            String json = redisTemplate.opsForList().leftPop("chat_buffer");
            
            if (json == null) {
                break; // Hết tin trong Redis thì dừng
            }
            jsonMessages.add(json);
        }

        // 2. Lưu xuống MongoDB
        if (!jsonMessages.isEmpty()) {
            List<Message> entities = new ArrayList<>();
            
            for (String json : jsonMessages) {
                try {
                    // Convert JSON String -> Object Message
                    Message msg = gson.fromJson(json, Message.class);
                    entities.add(msg);
                } catch (Exception e) {
                    System.err.println(" Lỗi format JSON: " + json);
                }
            }

            if (!entities.isEmpty()) {
                messageRepository.saveAll(entities); // Bulk Insert
                System.out.println(" SCHEDULER: Đa luu " + entities.size() + " tin nhắn xuong MongoDB.");
            }
        }
    }
}