package com.backend.backend.dto.conversation;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConversationResponseDto {
    private String id;
    private String title;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
