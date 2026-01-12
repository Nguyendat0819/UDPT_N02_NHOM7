    package com.example.chat.controller;
    import com.example.chat.model.User; 
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.security.crypto.password.PasswordEncoder;
    import org.springframework.stereotype.Controller;
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.PostMapping;
    import org.springframework.web.bind.annotation.RequestParam;

    import com.example.chat.repository.UserRepository;
    @Controller
    public class UserController {
        @Autowired
        private UserRepository userRepository;

        @Autowired
        private PasswordEncoder passwordEncoder; 
        @GetMapping("/register")
        public String ShowRegister(){
            return "register";
        }

        // xử lý đăng ký người dùng ở đây
        @PostMapping("/register")
        public String handleRegister(@RequestParam String username, 
                                    @RequestParam String password, 
                                    @RequestParam String email,
                                    // 1. Thêm required = false để không bắt buộc phải có file
                                    @RequestParam(value = "file", required = false) org.springframework.web.multipart.MultipartFile file) {
            
            // 2. Kiểm tra email tồn tại (Tránh lỗi 500 Duplicate Key)
            // Lưu ý: Bạn cần thêm hàm findByEmail trong UserRepository trước nhé
            /* if (userRepository.findByEmail(email) != null) {
                return "redirect:/register?error=email_exists";
            } 
            */

            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));

            // 3. Logic xử lý file an toàn (kiểm tra null)
            // Logic xử lý file an toàn
            if (file != null && !file.isEmpty()) {
                try {
                    // Lưu vào thư mục static để web hiển thị được
                    String uploadDir = "src/main/resources/static/uploads/";
                    java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadDir);
                    
                    // Tạo thư mục nếu chưa có
                    if (!java.nio.file.Files.exists(uploadPath)) {
                        java.nio.file.Files.createDirectories(uploadPath);
                    }

                    // Tạo tên file duy nhất
                    String fileName = java.util.UUID.randomUUID() + "_" + file.getOriginalFilename();
                    java.nio.file.Path filePath = uploadPath.resolve(fileName);
                    
                    // Lưu file vật lý
                    java.nio.file.Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    
                    // Gán đường dẫn vào DB (Lưu ý đường dẫn web dùng /uploads/...)
                    user.setAvatarUrl("/uploads/" + fileName); 
                    
                    System.out.println("Đã lưu ảnh thành công: " + fileName); // In log để debug
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Không nhận được file hoặc file rỗng!");
            }
            try {
                userRepository.save(user);
            } catch (org.springframework.dao.DataIntegrityViolationException e) {
                
                
                // return "redirect:/register?error=duplicate_email";
            }

            return "redirect:/login";
        }
}
