    package com.example.chat.controller;
    import com.example.chat.model.User; 
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.security.crypto.password.PasswordEncoder;
    import org.springframework.stereotype.Controller;
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.PostMapping;
    import org.springframework.web.bind.annotation.RequestParam;
    import org.springframework.web.bind.annotation.ResponseBody;
    import org.springframework.ui.Model;
    import java.util.List;
    import com.example.chat.repository.UserRepository;
    import com.example.chat.service.UserService;
    import com.example.chat.service.FriendshipService;
    
    import java.security.Principal;
    @Controller
    public class UserController {
        @Autowired
        private UserRepository userRepository;

        @Autowired
        private PasswordEncoder passwordEncoder; 
        // Service để xử lý logic nghiệp vụ
        @Autowired 
        private UserService UserService;

        @Autowired
        private FriendshipService friendshipService;

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

        @GetMapping("/login")
        public String ShowLogin(){
            return "login";
        }

        // @PostMapping("/login")
        // public String handleLogin()
        @GetMapping("/home")
        public String ShowHome(Model model, Principal principal) {
            // 1. Lấy email từ session đăng nhập
            String email = principal.getName();
            
            // 2. Tìm user trong database
            User user = userRepository.findByEmail(email);

            // --- QUAN TRỌNG: Kiểm tra nếu database bị xóa mất user này ---
            if (user == null) {
                return "redirect:/logout"; // Đá văng ra bắt đăng nhập lại
            }
            // ------------------------------------------------------------

            // 3. Đưa user vào Model (Phải đặt tên là "currentUser" cho khớp với HTML)
            model.addAttribute("currentUser", user); 
            
            return "home";
        }

            // 3. API Chặn bạn bè
        // 1. API Lấy danh sách bạn bè
        @GetMapping("/api/users")
        @ResponseBody
        public List<User> getFriends(Principal principal) {
            return friendshipService.getFriendsList(principal.getName());
        }

        // 2. API Thêm bạn (QUAN TRỌNG: Đây là cái bạn đang thiếu)
        @PostMapping("/api/friends/add")
        @ResponseBody
        public String addFriend(@RequestParam String email, Principal principal) {
            return friendshipService.addFriend(principal.getName(), email);
        }

        // 3. API Chặn bạn bè
        @PostMapping("/api/friends/block")
        @ResponseBody
        public String blockUser(@RequestParam String email, Principal principal) {
            return friendshipService.blockUser(principal.getName(), email);
        }
}
