package com.backend.backend.mapper;

import com.backend.backend.dao.entities.Notification;
import com.backend.backend.dao.entities.User;
import com.backend.backend.dto.notification.NotificationResponseDto;
import com.backend.backend.dto.notification.NotificationResquestDto;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class NotificationMapper {
    public Notification toEntity(NotificationResquestDto dto, User user) {
        Notification notification = new Notification();
        notification.setMessage(dto.getMessage());
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUser(user);
        return notification;
    }


    public NotificationResponseDto toResponseDTO(Notification notification) {
        NotificationResponseDto dto = new NotificationResponseDto();
        dto.setId(notification.getId());
        dto.setMessage(notification.getMessage());
        dto.setRead(notification.isRead());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setUserId(notification.getUser().getId());
        dto.setUserName(notification.getUser().getName());
        return dto;
    }
}
