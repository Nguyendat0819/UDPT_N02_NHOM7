package com.example.chat.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;
import lombok.Data;
import java.io.Serializable;
import java.util.UUID;

@Data
@Embeddable
public class FriendshipId implements Serializable {
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "target_user_id")
    private UUID targetUserId;
}