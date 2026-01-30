package com.example.chat.service;

import com.example.chat.model.User;
import com.example.chat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    // Hàm xử lý đăng ký chính
    public void registerUser(String username, String password, String email, MultipartFile file) throws Exception {
        
        // 1. Kiểm tra Email đã tồn tại chưa (Logic nghiệp vụ)
        if (userRepository.findByEmail(email).isPresent()) {
            throw new Exception("duplicate_email"); // Ném lỗi để Controller bắt
        }

        // 2. Tạo đối tượng User
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password)); // Mã hóa mật khẩu ngay tại Service

        // 3. Xử lý file ảnh (Logic kỹ thuật)
        if (file != null && !file.isEmpty()) {
            try {
                // Đường dẫn lưu file (Dùng đường dẫn tương đối đến thư mục static của dự án)
                String uploadDir = "src/main/resources/static/uploads/";
                Path uploadPath = Paths.get(uploadDir);

                // Tạo thư mục nếu chưa tồn tại
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // Tạo tên file ngẫu nhiên để tránh trùng tên
                String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                Path filePath = uploadPath.resolve(fileName);

                // Copy file vào thư mục
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                // Gán đường dẫn web (/uploads/...) vào database
                user.setAvatarUrl( fileName);

            } catch (IOException e) {
                e.printStackTrace();
                throw new Exception("Lỗi khi lưu file ảnh");
            }
        }

        // 4. Lưu User vào Database
        userRepository.save(user);
    }


    // 🔴 2. HÀM TÌM ID (ĐÃ NÂNG CẤP CACHE REDIS)
    // public UUID findIdByEmail(String email) {
    //     // Tạo Key cho Redis (Ví dụ: "uuid:test@gmail.com")
    //     String redisKey = "uuid:" + email;

    //     // --- BƯỚC 1: HỎI REDIS TRƯỚC ---
    //     try {
    //         String cachedUuid = redisTemplate.opsForValue().get(redisKey);
    //         if (cachedUuid != null) {
    //             // ✅ Có trong cache -> Trả về luôn (Không tốn query DB)
    //             // System.out.println("🎯 Cache hit: " + email); // Bật dòng này nếu muốn test log
    //             return UUID.fromString(cachedUuid);
    //         }
    //     } catch (Exception e) {
    //         // Nếu Redis chết, log lỗi nhẹ nhưng KHÔNG được dừng chương trình -> Vẫn xuống DB
    //         System.err.println("⚠️ Redis lỗi: " + e.getMessage());
    //     }

    //     // --- BƯỚC 2: KHÔNG CÓ -> XUỐNG DB TÌM ---
    //     // System.out.println("🐢 Cache miss -> DB Query: " + email);
    //     User user = userRepository.findByEmail(email)
    //             .orElseThrow(() -> new RuntimeException("User not found"));
        
    //     UUID userId = user.getId();

    //     // --- BƯỚC 3: CÓ DỮ LIỆU -> LƯU NGƯỢC VÀO REDIS ---
    //     try {
    //         // Lưu vào Redis, hẹn giờ 1 tiếng (60 phút) tự xóa
    //         redisTemplate.opsForValue().set(redisKey, userId.toString(), 60, TimeUnit.MINUTES);
    //     } catch (Exception e) {
    //         System.err.println("⚠️ Không lưu được vào Redis: " + e.getMessage());
    //     }

    //     return userId;
    // }

    public UUID findIdByEmail(String email) {
        String redisKey = "uuid:" + email;

        // BƯỚC 1: HỎI REDIS
        try {
            String cachedUuid = redisTemplate.opsForValue().get(redisKey);
            if (cachedUuid != null) {
                // 🟢 MỞ COMMENT DÒNG NÀY RA:
                System.out.println(" Cache HIT (Lay tu RAM): " + email); 
                return UUID.fromString(cachedUuid);
            }
        } catch (Exception e) {
            System.err.println("⚠️ Redis lỗi: " + e.getMessage());
        }

        // BƯỚC 2: XUỐNG DB
        // 🟠 MỞ COMMENT DÒNG NÀY RA:
        System.out.println("🐢 Cache MISS (Phải xuống DB): " + email); 
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        UUID userId = user.getId();

        // BƯỚC 3: LƯU LẠI
        try {
            redisTemplate.opsForValue().set(redisKey, userId.toString(), 60, TimeUnit.MINUTES);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return userId;
    }
}