package com.backend.backend.service.serviceInterface;

import com.backend.backend.dto.notification.NotificationResquestDto;
import com.backend.backend.dto.notification.NotificationResponseDto;


import java.util.List;

public interface NotificationService {
    NotificationResponseDto create(NotificationResquestDto requestDTO);

    NotificationResponseDto findById(String id);

    List<NotificationResponseDto> findAll();

    List<NotificationResponseDto> findByUserId(String userId);

    List<NotificationResponseDto> findUnreadByUserId(String userId);

    NotificationResponseDto markAsRead(String id);

    void markAllAsReadByUserId(String userId);

    void delete(String id);
}
