package com.backend.backend.dto.githubrepository;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * Réponse publique d'un dépôt GitHub configuré.
 * Le token chiffré n'est JAMAIS inclus dans cette réponse.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GithubRepositoryResponseDto {

    private String id;
    private String userId;
    private String repoOwner;
    private String repoName;
    private String branch;
    @JsonProperty("isPrivate")
    private boolean isPrivate;
    /** True si un token chiffré est présent en base (sans l'exposer). */
    private boolean tokenStored;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
