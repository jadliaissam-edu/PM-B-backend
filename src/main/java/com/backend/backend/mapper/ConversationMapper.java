package com.backend.backend.mapper;

import com.backend.backend.dao.entities.Conversation;
import com.backend.backend.dto.conversation.ConversationRequestDto;
import com.backend.backend.dto.conversation.ConversationResponseDto;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConversationMapper {

    @Autowired
    private ModelMapper modelMapper;

    public ConversationResponseDto toResponseDto(Conversation conversation) {
        if (conversation == null) {
            return null;
        }
        return modelMapper.map(conversation, ConversationResponseDto.class);
    }

    public Conversation toEntity(ConversationRequestDto requestDto) {
        if (requestDto == null) {
            return null;
        }
        return modelMapper.map(requestDto, Conversation.class);
    }
}
