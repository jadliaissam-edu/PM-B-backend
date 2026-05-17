package com.backend.backend.web;

import com.backend.backend.dao.entities.User;
import com.backend.backend.dao.repositories.UserRepository;
import com.backend.backend.service.serviceInterface.IAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final IAuthService authService;
    private final UserRepository userRepository;

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile() {
        return ResponseEntity.ok(toProfileResponse(authService.getCurrentUser()));
    }

    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestBody Map<String, Object> payload) {
        User user = authService.getCurrentUser();

        String firstName = normalize(payload.get("firstName"));
        String lastName = normalize(payload.get("lastName"));
        Boolean mfaEnabled = normalizeBoolean(payload.get("mfaEnabled"));

        if (firstName != null) {
            user.setFirstName(firstName);
        }
        if (lastName != null) {
            user.setLastName(lastName);
        }
        if (mfaEnabled != null) {
            user.setMfaEnabled(mfaEnabled);
            if (!mfaEnabled) {
                user.setMfaChallengeId(null);
                user.setMfaOtpCodeHash(null);
                user.setMfaOtpExpiresAt(null);
                user.setMfaOtpAttempts(null);
            }
        }

        user.setName(user.getFirstName() + " " + user.getLastName());
        User saved = userRepository.save(user);
        return ResponseEntity.ok(toProfileResponse(saved));
    }

    private Map<String, Object> toProfileResponse(User user) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", user.getId());
        response.put("firstName", user.getFirstName());
        response.put("lastName", user.getLastName());
        response.put("email", user.getEmail());
        response.put("role", user.getRole());
        response.put("avatarUrl", user.getAvatarUrl());
        response.put("mfaEnabled", user.getMfaEnabled());
        return response;
    }

    private String normalize(Object value) {
        if (value == null) {
            return null;
        }
        String text = value.toString().trim();
        return text.isEmpty() ? null : text;
    }

    private Boolean normalizeBoolean(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        String text = value.toString().trim();
        if (text.isEmpty()) {
            return null;
        }
        return Boolean.parseBoolean(text);
    }
}