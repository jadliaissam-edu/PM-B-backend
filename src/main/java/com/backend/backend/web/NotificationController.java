package com.backend.backend.web;

import com.backend.backend.dto.notification.NotificationResponseDto;
import com.backend.backend.dto.notification.NotificationResquestDto;
import com.backend.backend.service.serviceInterface.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService; // injection via l'interface

    // POST /api/notifications
    @PostMapping
    public ResponseEntity<NotificationResponseDto> create(
            @RequestBody NotificationResquestDto requestDTO) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(notificationService.create(requestDTO));
    }

    // GET /api/notifications/{id}
    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponseDto> findById(@PathVariable String id) {
        return ResponseEntity.ok(notificationService.findById(id));
    }

    // GET /api/notifications
    @GetMapping
    public ResponseEntity<List<NotificationResponseDto>> findAll() {
        return ResponseEntity.ok(notificationService.findAll());
    }

    // GET /api/notifications/user/{userId}
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponseDto>> findByUser(
            @PathVariable String userId) {
        return ResponseEntity.ok(notificationService.findByUserId(userId));
    }

    // GET /api/notifications/user/{userId}/unread
    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<NotificationResponseDto>> findUnreadByUser(
            @PathVariable String userId) {
        return ResponseEntity.ok(notificationService.findUnreadByUserId(userId));
    }

    // PATCH /api/notifications/{id}/read
    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponseDto> markAsRead(@PathVariable String id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    // PATCH /api/notifications/user/{userId}/read-all
    @PatchMapping("/user/{userId}/read-all")
    public ResponseEntity<Void> markAllAsRead(@PathVariable String userId) {
        notificationService.markAllAsReadByUserId(userId);
        return ResponseEntity.noContent().build();
    }

    // DELETE /api/notifications/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        notificationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
