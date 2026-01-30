package com.example.chat.controller;

import com.example.chat.model.User;
import com.example.chat.repository.FriendshipRepository;
import com.example.chat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class FriendController {

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private UserService userService;

    // API 1: Lấy danh sách những người mình đã chặn
    @GetMapping("/users/blocked")
    public ResponseEntity<List<User>> getBlockedUsers(Principal principal) {
        UUID myId = userService.findIdByEmail(principal.getName());
        
        // Gọi hàm số 2 trong Repository
        List<User> blockedUsers = friendshipRepository.findBlockedUsers(myId);
        
        return ResponseEntity.ok(blockedUsers);
    }

    // API 2: Bỏ chặn một người
    @PostMapping("/friends/unblock")
    public ResponseEntity<?> unblockUser(@RequestParam UUID targetId, Principal principal) {
        try {
            UUID myId = userService.findIdByEmail(principal.getName());
            
            // Gọi hàm số 3 trong Repository
            friendshipRepository.unblockUser(myId, targetId);
            
            return ResponseEntity.ok().body("{\"message\": \"Đã bỏ chặn thành công\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    // API 3 Hủy kết bạn
    // Gọi từ JS: /api/friends/unfriend?targetEmail=...
    @PostMapping("/friends/unfriend")
    public ResponseEntity<?> unfriendUser(@RequestParam String targetEmail, Principal principal) {
        try {
            // 1. Lấy ID của mình
            UUID myId = userService.findIdByEmail(principal.getName());
            
            // 2. Lấy ID của người muốn hủy kết bạn
            UUID targetId = userService.findIdByEmail(targetEmail);
            
            // 3. Xóa khỏi database
            friendshipRepository.deleteFriendship(myId, targetId);
            
            return ResponseEntity.ok().body("{\"message\": \"Đã hủy kết bạn thành công\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }
}