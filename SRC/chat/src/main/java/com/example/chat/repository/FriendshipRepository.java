package com.example.chat.repository;

import com.example.chat.model.Friendship;
import com.example.chat.model.FriendshipId;
import com.example.chat.model.User; // Nhớ import User
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying; // Import cho lệnh Xóa/Sửa
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional; // Import Transaction

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, FriendshipId> {

    // 1. Tìm kiếm trạng thái (Giữ nguyên của bạn)
    Optional<Friendship> findByIdUserIdAndIdTargetUserId(UUID userId, UUID targetUserId);

    // 2. LẤY DANH SÁCH USER BỊ CHẶN (Đã sửa để trả về User thay vì Friendship)
    // Dùng Native Query để join sang bảng users lấy luôn tên và avatar
    @Query(value = "SELECT u.* FROM users u " +
                   "JOIN friendships f ON u.id = f.target_user_id " +
                   "WHERE f.user_id = :userId AND f.status = 'chặn'", 
           nativeQuery = true)
    List<User> findBlockedUsers(@Param("userId") UUID userId);

    
    // 3. BỎ CHẶN (Sửa lại: Chỉ đổi trạng thái về 'bạn', KHÔNG XÓA)
    @Modifying
    @Transactional
    @Query(value = "UPDATE friendships SET status = 'bạn' WHERE user_id = :userId AND target_user_id = :targetId", 
           nativeQuery = true)
    void unblockUser(@Param("userId") UUID userId, @Param("targetId") UUID targetId);

    // 4. KIỂM TRA CHẶN (Giữ nguyên logic của bạn nhưng đảm bảo đúng cú pháp JPQL)
    @Query("SELECT COUNT(f) > 0 FROM Friendship f " +
           "WHERE f.id.userId = :recipientId " +
           "AND f.id.targetUserId = :senderId " +
           "AND f.status = 'chặn'")
    boolean isSenderBlockedByRecipient(@Param("recipientId") UUID recipientId, @Param("senderId") UUID senderId);

    //  5. HỦY KẾT BẠN (Xóa quan hệ từ cả 2 phía cho sạch sẽ)
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM friendships " +
                   "WHERE (user_id = :userId AND target_user_id = :targetId) " +
                   "OR (user_id = :targetId AND target_user_id = :userId)", 
           nativeQuery = true)
    void deleteFriendship(@Param("userId") UUID userId, @Param("targetId") UUID targetId);
    
    //6. Lấy danh sách bạn bè (Giữ nguyên)
    @Query("SELECT f FROM Friendship f WHERE f.id.userId = :userId AND f.status = 'bạn'")
    List<Friendship> findFriends(@Param("userId") UUID userId);
}