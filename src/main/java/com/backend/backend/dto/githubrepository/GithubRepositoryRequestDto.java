package com.backend.backend.dto.githubrepository;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Corps de la requête d'ajout / mise à jour d'un dépôt GitHub.
 * Le token PAT (github_token) est reçu en clair depuis le service IA Python,
 * qui l'a déjà chiffré avec Fernet. Le backend stocke la version chiffrée telle quelle.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GithubRepositoryRequestDto {

    /** Identifiant de l'utilisateur propriétaire. */
    private String userId;

    /** Login GitHub du propriétaire (ex: "facebook"). */
    private String repoOwner;

    /** Nom du dépôt (ex: "react"). */
    private String repoName;

    /** Branche cible (ex: "main"). */
    private String branch;

    /** True si le dépôt est privé. */
    private boolean isPrivate;

    /**
     * Token GitHub chiffré (Fernet) par le service IA Python.
     * Null pour les dépôts publics.
     */
    private String githubTokenEncrypted;
}
