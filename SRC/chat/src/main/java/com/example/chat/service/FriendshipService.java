package com.example.chat.service;

import com.example.chat.model.Friendship;
import com.example.chat.model.FriendshipId;
import com.example.chat.model.User;
import com.example.chat.repository.FriendshipRepository;
import com.example.chat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FriendshipService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendshipRepository friendshipRepository;

    // 1. Logic lấy danh sách bạn bè (Đã tối ưu bằng @Query)
    public List<User> getFriendsList(String email) {
        User currentUser = userRepository.findByEmail(email);
        
        // Gọi hàm tìm kiếm nhanh từ Repository
        List<Friendship> friendships = friendshipRepository.findFriends(currentUser.getId());
        
        List<UUID> friendIds = new ArrayList<>();
        for (Friendship f : friendships) {
            friendIds.add(f.getId().getTargetUserId());
        }
        
        return userRepository.findAllById(friendIds);
    }

    // 2. Logic thêm bạn mới (Có kiểm tra kỹ hơn)
    public String addFriend(String currentEmail, String targetEmail) {
        User currentUser = userRepository.findByEmail(currentEmail);
        User targetUser = userRepository.findByEmail(targetEmail);

        if (targetUser == null) return "Email không tồn tại!";
        if (currentUser.getId().equals(targetUser.getId())) return "Không thể tự kết bạn!";

        // Kiểm tra xem quan hệ đã tồn tại chưa (bất kể là bạn hay chặn)
        if (friendshipRepository.findByIdUserIdAndIdTargetUserId(currentUser.getId(), targetUser.getId()).isPresent()) {
            return "Đã có quan hệ bạn bè hoặc đang bị chặn!";
        }

        saveFriendshipToDB(currentUser.getId(), targetUser.getId());
        saveFriendshipToDB(targetUser.getId(), currentUser.getId());

        return "ok";
    }

    private void saveFriendshipToDB(UUID userId, UUID targetId) {
        FriendshipId id = new FriendshipId();
        id.setUserId(userId);
        id.setTargetUserId(targetId);

        Friendship friendship = new Friendship();
        friendship.setId(id);
        friendship.setStatus("bạn"); 

        friendshipRepository.save(friendship);
    }

    // 3. Logic chặn người dùng
    public String blockUser(String currentEmail, String targetEmail) {
        User currentUser = userRepository.findByEmail(currentEmail);
        User targetUser = userRepository.findByEmail(targetEmail);

        if (targetUser == null) return "User not found";

        // Tìm quan hệ hiện tại
        // Lưu ý: FriendshipId gồm (userId, targetId)
        FriendshipId id = new FriendshipId();
        id.setUserId(currentUser.getId());
        id.setTargetUserId(targetUser.getId());

        Friendship friendship = friendshipRepository.findById(id).orElse(null);
        
        if (friendship != null) {
            friendship.setStatus("chặn"); // Đổi trạng thái thành CHẶN
            friendshipRepository.save(friendship);
            return "ok";
        }
        return "Chưa kết bạn nên không thể chặn!";
    }
}