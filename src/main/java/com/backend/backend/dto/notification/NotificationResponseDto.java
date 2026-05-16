package com.backend.backend.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponseDto {
    private String id;
    private String message;
    private boolean isRead;
    private LocalDateTime createdAt;
    private String userId;
    private String userName;
}
