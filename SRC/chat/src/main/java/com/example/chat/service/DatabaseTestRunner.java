// package com.example.chat.service;


// import com.example.chat.repository.UserRepository;
// import com.example.chat.repository.MessageRepository;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.CommandLineRunner;
// import org.springframework.stereotype.Component;


// @Component
// public class DatabaseTestRunner implements CommandLineRunner {

//     @Autowired
//     private UserRepository userRepository;

//     @Autowired
//     private MessageRepository messageRepository;

//     @Override
//     public void run(String... args) throws Exception {
//         System.out.println("========== KIỂM TRA KẾT NỐI DATABASE ==========");
        
//         try {
//             // 1. Test PostgreSQL
//             long userCount = userRepository.count();
//             System.out.println("✅ Kết nối PostgreSQL thành công! Số lượng User: " + userCount);
//         } catch (Exception e) {
//             System.err.println("❌ Lỗi kết nối PostgreSQL: " + e.getMessage());
//         }

//         try {
//             // 2. Test MongoDB
//             long messageCount = messageRepository.count();
//             System.out.println("✅ Kết nối MongoDB thành công! Số lượng Tin nhắn: " + messageCount);
//         } catch (Exception e) {
//             System.err.println("❌ Lỗi kết nối MongoDB: " + e.getMessage());
//         }
        
//         System.out.println("===============================================");
//     }
// }