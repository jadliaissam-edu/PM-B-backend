package com.backend.backend.service.manager;

import com.backend.backend.dto.notification.NotificationResquestDto;
import com.backend.backend.dto.notification.NotificationResponseDto;
import com.backend.backend.dao.entities.Notification;
import com.backend.backend.dao.entities.User;
import com.backend.backend.mapper.NotificationMapper;
import com.backend.backend.dao.repositories.NotificationRepository;
import com.backend.backend.dao.repositories.UserRepository;
import com.backend.backend.service.serviceInterface.NotificationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationManager implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    // ─── CREATE ───────────────────────────────────────────────────────────────

    @Override
    public NotificationResponseDto create(NotificationResquestDto requestDTO) {
        User user = userRepository.findById(requestDTO.getUserId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "User not found with id: " + requestDTO.getUserId()));

        Notification notification = notificationMapper.toEntity(requestDTO, user);
        Notification saved = notificationRepository.save(notification);
        return notificationMapper.toResponseDTO(saved);
    }

    // ─── READ ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public NotificationResponseDto findById(String id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Notification not found with id: " + id));
        return notificationMapper.toResponseDTO(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponseDto> findAll() {
        return notificationRepository.findAll()
                .stream()
                .map(notificationMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponseDto> findByUserId(String userId) {
        return notificationRepository.findByUserId(userId)
                .stream()
                .map(notificationMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponseDto> findUnreadByUserId(String userId) {
        return notificationRepository.findByUserIdAndIsReadFalse(userId)
                .stream()
                .map(notificationMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    // ─── UPDATE ───────────────────────────────────────────────────────────────

    @Override
    public NotificationResponseDto markAsRead(String id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Notification not found with id: " + id));
        notification.setRead(true);
        Notification updated = notificationRepository.save(notification);
        return notificationMapper.toResponseDTO(updated);
    }

    @Override
    public void markAllAsReadByUserId(String userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndIsReadFalse(userId);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    // ─── DELETE ───────────────────────────────────────────────────────────────

    @Override
    public void delete(String id) {
        if (!notificationRepository.existsById(id)) {
            throw new EntityNotFoundException("Notification not found with id: " + id);
        }
        notificationRepository.deleteById(id);
    }
}