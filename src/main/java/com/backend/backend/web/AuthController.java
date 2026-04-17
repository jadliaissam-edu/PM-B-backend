package com.backend.backend.web;

import com.backend.backend.dto.auth.AuthResponseDto;
import com.backend.backend.dto.auth.LoginRequestDto;
import com.backend.backend.dto.auth.LogoutRequestDto;
import com.backend.backend.dto.auth.MfaVerifyRequestDto;
import com.backend.backend.dto.auth.RefreshTokenRequestDto;
import com.backend.backend.dto.auth.RegisterRequestDto;
import com.backend.backend.dto.auth.TokenResponseDto;
import com.backend.backend.service.serviceInterface.IAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@RequestBody RegisterRequestDto request) {
        AuthResponseDto response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody LoginRequestDto request) {
        AuthResponseDto response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/mfa/verify")
    public ResponseEntity<AuthResponseDto> verifyMfa(@RequestBody MfaVerifyRequestDto request) {
        AuthResponseDto response = authService.verifyMfa(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDto> refresh(@RequestBody RefreshTokenRequestDto request) {
        TokenResponseDto response = authService.refresh(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestBody(required = false) LogoutRequestDto request) {
        authService.logout(request);
        return ResponseEntity.ok(Map.of("message", "Logout successful."));
    }
}
