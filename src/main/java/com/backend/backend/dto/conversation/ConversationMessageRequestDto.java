package com.backend.backend.dto.conversation;

import lombok.Data;

@Data
public class ConversationMessageRequestDto {
    private String role;
    private String content;
}
