package com.example.chat.repository;

import com.example.chat.model.Conversation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface ConversationRepository extends MongoRepository<Conversation, String> {
    // Tìm phòng chat giữa 2 người cụ thể
    @org.springframework.data.mongodb.repository.Query("{ 'participants': { $all: [?0, ?1] } }")
    Optional<Conversation> findConversationByParticipants(UUID user1, UUID user2);
}