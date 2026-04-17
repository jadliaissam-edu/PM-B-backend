package com.backend.backend.mapper;

import com.backend.backend.dao.entities.ConversationMessage;
import com.backend.backend.dto.conversation.ConversationMessageRequestDto;
import com.backend.backend.dto.conversation.ConversationMessageResponseDto;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConversationMessageMapper {

    @Autowired
    private ModelMapper modelMapper;

    public ConversationMessageResponseDto toResponseDto(ConversationMessage message) {
        if (message == null) {
            return null;
        }
        return modelMapper.map(message, ConversationMessageResponseDto.class);
    }

    public ConversationMessage toEntity(ConversationMessageRequestDto requestDto) {
        if (requestDto == null) {
            return null;
        }
        return modelMapper.map(requestDto, ConversationMessage.class);
    }
}
