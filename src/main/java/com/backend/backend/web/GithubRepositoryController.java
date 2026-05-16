package com.backend.backend.web;

import com.backend.backend.dto.githubrepository.GithubRepositoryRequestDto;
import com.backend.backend.dto.githubrepository.GithubRepositoryResponseDto;
import com.backend.backend.service.serviceInterface.IGithubRepositoryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller pour la gestion des dépôts GitHub configurés par les utilisateurs.
 *
 * Base URL : /api/repos
 *
 * Appelé principalement par le service IA Python (PM-B-ia) pour persister
 * les dépôts et récupérer les tokens chiffrés lors des analyses.
 */
@RestController
@RequestMapping("/api/repos")
@RequiredArgsConstructor
public class GithubRepositoryController {

    private final IGithubRepositoryService githubRepositoryService;

    /**
     * POST /api/repos/upsert
     * Ajoute ou met à jour un dépôt pour un utilisateur.
     * Appelé par le service IA après validation + chiffrement du PAT.
     */
    @PostMapping("/upsert")
    public ResponseEntity<GithubRepositoryResponseDto> upsertRepository(
        @RequestBody GithubRepositoryRequestDto request
    ) {
        GithubRepositoryResponseDto result = githubRepositoryService.upsertRepository(request);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/repos/{userId}
     * Retourne tous les dépôts configurés pour un utilisateur.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<List<GithubRepositoryResponseDto>> getRepositoriesByUser(
        @PathVariable String userId
    ) {
        return ResponseEntity.ok(githubRepositoryService.getRepositoriesByUser(userId));
    }

    /**
     * GET /api/repos/{userId}/{repoOwner}/{repoName}
     * Retourne un dépôt spécifique.
     */
    @GetMapping("/{userId}/{repoOwner}/{repoName}")
    public ResponseEntity<GithubRepositoryResponseDto> getRepository(
        @PathVariable String userId,
        @PathVariable String repoOwner,
        @PathVariable String repoName
    ) {
        return githubRepositoryService.getRepository(userId, repoOwner, repoName)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/repos/{userId}/{repoOwner}/{repoName}/token
     * Retourne uniquement le token chiffré (usage interne : service IA Python).
     * Cette route est destinée à être appelée uniquement entre services backend.
     */
    @GetMapping("/{userId}/{repoOwner}/{repoName}/token")
    public ResponseEntity<Map<String, String>> getEncryptedToken(
        @PathVariable String userId,
        @PathVariable String repoOwner,
        @PathVariable String repoName
    ) {
        return githubRepositoryService.getEncryptedToken(userId, repoOwner, repoName)
            .map(token -> ResponseEntity.ok(Map.of("encryptedToken", token)))
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/repos/{userId}/{repoOwner}/{repoName}
     * Supprime un dépôt de la configuration d'un utilisateur.
     */
    @DeleteMapping("/{userId}/{repoOwner}/{repoName}")
    public ResponseEntity<Map<String, String>> deleteRepository(
        @PathVariable String userId,
        @PathVariable String repoOwner,
        @PathVariable String repoName
    ) {
        try {
            githubRepositoryService.deleteRepository(userId, repoOwner, repoName);
            return ResponseEntity.ok(Map.of(
                "status", "ok",
                "message", String.format("Dépôt '%s/%s' supprimé.", repoOwner, repoName)
            ));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }
}
