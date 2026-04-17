package com.backend.backend.dao.repositories;

import com.backend.backend.dao.entities.Conversation;
import com.backend.backend.dao.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation, String> {
    List<Conversation> findAllByUserOrderByUpdatedAtDesc(User user);
}
