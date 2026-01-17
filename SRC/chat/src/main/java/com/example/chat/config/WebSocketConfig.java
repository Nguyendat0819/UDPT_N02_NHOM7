package com.example.chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // Bật tính năng Message Broker (người đưa thư)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 1. Điểm kết nối (Handshake)
        // Client sẽ kết nối vào đường dẫn: http://localhost:8080/ws
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Cho phép kết nối từ mọi nguồn (để test cho dễ)
                .withSockJS(); // Hỗ trợ fallback nếu trình duyệt cũ không có WebSocket
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 2. Cấu hình đường đi của tin nhắn

        // Prefix cho các tin nhắn từ Server gửi xuống Client
        // /topic: Dùng cho chat nhóm (Public)
        // /user: Dùng cho chat riêng (Private)
        registry.enableSimpleBroker("/topic", "/queue");

        // Prefix cho các tin nhắn từ Client gửi lên Server
        // Ví dụ: Client gửi vào "/app/chat" -> Server sẽ xử lý
        registry.setApplicationDestinationPrefixes("/app");
        
        // Prefix dành riêng cho user cụ thể (gửi tin nhắn riêng)
        registry.setUserDestinationPrefix("/user");
    }
}