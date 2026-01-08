package com.example.chat.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "friendships")
@Data
public class Friendship {

    @EmbeddedId // ĐÂY LÀ DÒNG QUAN TRỌNG NHẤT ĐỂ SỬA LỖI
    private FriendshipId id;

    @Column(nullable = false)
    private String status; // 'bạn' hoặc 'chặn'
}