package com.backend.backend.dao.repositories;

import com.backend.backend.dao.entities.Conversation;
import com.backend.backend.dao.entities.ConversationMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConversationMessageRepository extends JpaRepository<ConversationMessage, String> {
    List<ConversationMessage> findAllByConversationOrderByCreatedAtAsc(Conversation conversation);

    boolean existsByConversation(Conversation conversation);
}
