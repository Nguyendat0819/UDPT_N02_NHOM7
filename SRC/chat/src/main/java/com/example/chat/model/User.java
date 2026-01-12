package com.example.chat.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Column;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    private UUID id;

    private String email;
    private String password;
    private String username;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "created_at", updatable = false)
    private java.time.LocalDateTime createdAt; // Khớp với created_at trong pgAdmin

    // Tự động chạy trước khi lưu vào PostgreSQL
    @jakarta.persistence.PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = java.util.UUID.randomUUID(); // Tự sinh ID UUID
        }
        // Sửa lại dòng lỗi cú pháp này
        if (this.createdAt == null) { 
            this.createdAt = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        }
    }
}
