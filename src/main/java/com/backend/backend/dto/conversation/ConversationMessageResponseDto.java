package com.backend.backend.dto.conversation;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConversationMessageResponseDto {
    private String id;
    private String role;
    private String content;
    private LocalDateTime createdAt;
}
