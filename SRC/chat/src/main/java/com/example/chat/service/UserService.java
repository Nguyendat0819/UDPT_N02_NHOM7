package com.example.chat.service;

import com.example.chat.model.User;
import com.example.chat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Hàm xử lý đăng ký chính
    public void registerUser(String username, String password, String email, MultipartFile file) throws Exception {
        
        // 1. Kiểm tra Email đã tồn tại chưa (Logic nghiệp vụ)
        if (userRepository.findByEmail(email) != null) {
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
                user.setAvatarUrl("/uploads/" + fileName);

            } catch (IOException e) {
                e.printStackTrace();
                throw new Exception("Lỗi khi lưu file ảnh");
            }
        }

        // 4. Lưu User vào Database
        userRepository.save(user);
    }
}