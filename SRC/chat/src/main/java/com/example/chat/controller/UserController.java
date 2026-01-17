    package com.example.chat.controller;
    import com.example.chat.model.User; 
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.security.crypto.password.PasswordEncoder;
    import org.springframework.stereotype.Controller;
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.PostMapping;
    import org.springframework.web.bind.annotation.RequestParam;

    import com.example.chat.repository.UserRepository;
    import com.example.chat.service.UserService;
    @Controller
    public class UserController {
        @Autowired
        private UserRepository userRepository;

        @Autowired
        private PasswordEncoder passwordEncoder; 
        // Service để xử lý logic nghiệp vụ
        @Autowired 
        private UserService UserService;

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
            
            try {
                UserService.registerUser(username, password, email, file);
                return "redirect:/login";
            }catch (Exception e) {
                // Xử lý lỗi (ví dụ: email trùng)
                return "redirect:/register?error=true";
            }
        }
}
