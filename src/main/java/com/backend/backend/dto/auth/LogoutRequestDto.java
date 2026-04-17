package com.backend.backend.dto.auth;

import lombok.Data;

@Data
public class LogoutRequestDto {
    private String refreshToken;
}
