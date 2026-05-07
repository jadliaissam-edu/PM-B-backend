package com.backend.backend.web;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
public class GitHubOAuthController {

    @Value("${github.oauth.client-id}")
    private String clientId;

    @Value("${github.oauth.client-secret}")
    private String clientSecret;

    /**
     * Échange un code OAuth GitHub contre un access_token.
     * Appelé par le frontend après que GitHub a redirigé vers /github/callback?code=XXX
     */
    @PostMapping("/oauth/exchange")
    public ResponseEntity<Map<String, String>> exchangeCode(@RequestParam String code) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", "application/json");

        Map<String, String> body = Map.of(
                "client_id", clientId,
                "client_secret", clientSecret,
                "code", code
        );

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://github.com/login/oauth/access_token",
                    request,
                    Map.class
            );

            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = (Map<String, Object>) response.getBody();

            if (responseBody == null || responseBody.containsKey("error")) {
                String error = responseBody != null
                        ? (String) responseBody.get("error_description")
                        : "Unknown error";
                return ResponseEntity.badRequest()
                        .body(Map.of("error", error != null ? error : "OAuth exchange failed"));
            }

            String accessToken = (String) responseBody.get("access_token");
            String scope       = (String) responseBody.getOrDefault("scope", "");
            String tokenType   = (String) responseBody.getOrDefault("token_type", "bearer");

            return ResponseEntity.ok(Map.of(
                    "access_token", accessToken,
                    "scope", scope,
                    "token_type", tokenType
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de l'échange OAuth : " + e.getMessage()));
        }
    }

    /**
     * Retourne le client_id public pour que le frontend construise l'URL d'autorisation.
     */
    @GetMapping("/oauth/client-id")
    public ResponseEntity<Map<String, String>> getClientId() {
        return ResponseEntity.ok(Map.of("client_id", clientId));
    }
}
