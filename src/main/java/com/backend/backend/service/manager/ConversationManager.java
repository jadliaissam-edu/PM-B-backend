package com.backend.backend.service.manager;

import com.backend.backend.dao.entities.Conversation;
import com.backend.backend.dao.entities.ConversationMessage;
import com.backend.backend.dao.entities.User;
import com.backend.backend.dao.repositories.ConversationMessageRepository;
import com.backend.backend.dao.repositories.ConversationRepository;
import com.backend.backend.dto.conversation.ConversationMessageRequestDto;
import com.backend.backend.dto.conversation.ConversationMessageResponseDto;
import com.backend.backend.dto.conversation.ConversationRequestDto;
import com.backend.backend.dto.conversation.ConversationResponseDto;
import com.backend.backend.mapper.ConversationMapper;
import com.backend.backend.mapper.ConversationMessageMapper;
import com.backend.backend.service.serviceInterface.IAuthService;
import com.backend.backend.service.serviceInterface.IConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConversationManager implements IConversationService {

    private final ConversationRepository conversationRepository;
    private final ConversationMessageRepository conversationMessageRepository;
    private final ConversationMapper conversationMapper;
    private final ConversationMessageMapper conversationMessageMapper;
    private final IAuthService authService;

    @Override
    public ConversationResponseDto createConversation(ConversationRequestDto requestDto) {
        User user = authService.getCurrentUser();

        Conversation conversation = conversationMapper.toEntity(requestDto);
        if (conversation == null) {
            conversation = new Conversation();
        }

        if (conversation.getTitle() == null || conversation.getTitle().isBlank()) {
            conversation.setTitle("Nouvelle conversation");
        }

        LocalDateTime now = LocalDateTime.now();
        conversation.setUser(user);
        conversation.setCreatedAt(now);
        conversation.setUpdatedAt(now);

        Conversation saved = conversationRepository.save(conversation);
        return conversationMapper.toResponseDto(saved);
    }

    @Override
    public List<ConversationResponseDto> getConversationsByCurrentUser() {
        User user = authService.getCurrentUser();

        return conversationRepository.findAllByUserOrderByUpdatedAtDesc(user)
                .stream()
                .filter(conversationMessageRepository::existsByConversation)
                .map(conversationMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public ConversationResponseDto updateConversationTitle(String conversationId, ConversationRequestDto requestDto) {
        Conversation conversation = getOwnedConversationOrThrow(conversationId);

        String nextTitle = requestDto != null ? requestDto.getTitle() : null;
        if (nextTitle == null || nextTitle.isBlank()) {
            nextTitle = "Nouvelle conversation";
        }

        conversation.setTitle(nextTitle.trim());
        conversation.setUpdatedAt(LocalDateTime.now());

        Conversation saved = conversationRepository.save(conversation);
        return conversationMapper.toResponseDto(saved);
    }

    @Override
    public List<ConversationMessageResponseDto> getMessagesByConversation(String conversationId, Integer limit) {
        Conversation conversation = getOwnedConversationOrThrow(conversationId);

        List<ConversationMessage> conversationMessages = conversationMessageRepository
                .findAllByConversationOrderByCreatedAtAsc(conversation);

        if (limit != null && limit > 0 && conversationMessages.size() > limit) {
            conversationMessages = conversationMessages.subList(conversationMessages.size() - limit, conversationMessages.size());
        }

        return conversationMessages
                .stream()
                .map(conversationMessageMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public ConversationMessageResponseDto addMessage(String conversationId, ConversationMessageRequestDto requestDto) {
        Conversation conversation = getOwnedConversationOrThrow(conversationId);

        if (requestDto == null || requestDto.getContent() == null || requestDto.getContent().isBlank()) {
            throw new IllegalArgumentException("content is required");
        }

        ConversationMessage message = conversationMessageMapper.toEntity(requestDto);
        if (message == null) {
            message = new ConversationMessage();
        }

        message.setRole(resolveRole(requestDto.getRole()));
        message.setContent(requestDto.getContent());
        message.setCreatedAt(LocalDateTime.now());
        message.setConversation(conversation);

        ConversationMessage savedMessage = conversationMessageRepository.save(message);

        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        return conversationMessageMapper.toResponseDto(savedMessage);
    }

    @Override
    public void deleteConversation(String conversationId) {
        Conversation conversation = getOwnedConversationOrThrow(conversationId);
        conversationRepository.delete(conversation);
    }

    private Conversation getOwnedConversationOrThrow(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            throw new IllegalArgumentException("conversationId is required");
        }

        User currentUser = authService.getCurrentUser();

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation introuvable"));

        // Controle d'acces: un utilisateur ne peut lire/editer que ses propres conversations.
        if (conversation.getUser() == null || !conversation.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Conversation introuvable");
        }

        return conversation;
    }

    private String resolveRole(String role) {
        if (role == null || role.isBlank()) {
            return "user";
        }

        String normalized = role.trim().toLowerCase(Locale.ROOT);
        if (!normalized.equals("user") && !normalized.equals("assistant") && !normalized.equals("system")) {
            throw new IllegalArgumentException("role must be user, assistant or system");
        }
        return normalized;
    }
}
