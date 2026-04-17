package com.backend.backend.service.serviceInterface;

import com.backend.backend.dto.conversation.ConversationMessageRequestDto;
import com.backend.backend.dto.conversation.ConversationMessageResponseDto;
import com.backend.backend.dto.conversation.ConversationRequestDto;
import com.backend.backend.dto.conversation.ConversationResponseDto;

import java.util.List;

public interface IConversationService {
    ConversationResponseDto createConversation(ConversationRequestDto requestDto);

    List<ConversationResponseDto> getConversationsByCurrentUser();

    List<ConversationMessageResponseDto> getMessagesByConversation(String conversationId, Integer limit);

    ConversationResponseDto updateConversationTitle(String conversationId, ConversationRequestDto requestDto);

    ConversationMessageResponseDto addMessage(String conversationId, ConversationMessageRequestDto requestDto);

    void deleteConversation(String conversationId);
}
