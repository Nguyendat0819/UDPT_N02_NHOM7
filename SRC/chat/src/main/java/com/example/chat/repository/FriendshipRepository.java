package com.example.chat.repository;

import com.example.chat.model.Friendship;
import com.example.chat.model.FriendshipId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, FriendshipId> {

    // 1. Tìm kiếm trạng thái quan hệ dựa trên ID người gửi và người nhận
    // Spring sẽ tự động bóc tách FriendshipId để tìm kiếm
    Optional<Friendship> findByIdUserIdAndIdTargetUserId(UUID userId, UUID targetUserId);

    // 2. Lấy danh sách những người mà User A đã "chặn"
    @Query("SELECT f FROM Friendship f WHERE f.id.userId = :userId AND f.status = 'chặn'")
    List<Friendship> findBlockedUsers(@Param("userId") UUID userId);

    // 3. Lấy danh sách bạn bè của User A (để gợi ý tên khi nhắn tin)
    @Query("SELECT f FROM Friendship f WHERE f.id.userId = :userId AND f.status = 'bạn'")
    List<Friendship> findFriends(@Param("userId") UUID userId);

    // 4. KIỂM TRA CHẶN (Cực kỳ quan trọng cho Chương 6)
    // Kiểm tra xem Người Nhận có đang chặn Người Gửi hay không
    @Query("SELECT COUNT(f) > 0 FROM Friendship f " +
           "WHERE f.id.userId = :recipientId " +
           "AND f.id.targetUserId = :senderId " +
           "AND f.status = 'chặn'")
    boolean isSenderBlockedByRecipient(@Param("recipientId") UUID recipientId, @Param("senderId") UUID senderId);
}