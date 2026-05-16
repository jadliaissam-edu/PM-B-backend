package com.backend.backend.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResquestDto {
    @NotBlank(message = "Message is required")
    private String message;

    @NotNull(message = "User ID is required")
    private String userId;
}
