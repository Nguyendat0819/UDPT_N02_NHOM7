package com.example.chat.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Column;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String email;
    private String password;
    private String username;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "created_at", updatable = false)
    private java.time.LocalDateTime createdAt; // Khớp với created_at trong pgAdmin

    // Tự động chạy trước khi lưu vào PostgreSQL
    @Version
    private Long version;
    // @jakarta.persistence.PrePersist
    @PrePersist
    protected void onCreate() {
        // Sửa lại dòng lỗi cú pháp này
        if (this.createdAt == null) { 
            this.createdAt = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        }
    }
}
