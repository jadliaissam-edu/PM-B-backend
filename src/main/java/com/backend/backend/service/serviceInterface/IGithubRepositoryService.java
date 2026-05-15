package com.backend.backend.service.serviceInterface;

import com.backend.backend.dto.githubrepository.GithubRepositoryRequestDto;
import com.backend.backend.dto.githubrepository.GithubRepositoryResponseDto;

import java.util.List;
import java.util.Optional;

/**
 * Contrat du service de gestion des dépôts GitHub configurés par les utilisateurs.
 */
public interface IGithubRepositoryService {

    /**
     * Ajoute ou met à jour (upsert) un dépôt pour un utilisateur.
     * Le token chiffré fourni remplace l'ancien s'il est présent.
     */
    GithubRepositoryResponseDto upsertRepository(GithubRepositoryRequestDto request);

    /** Retourne tous les dépôts configurés pour un utilisateur. */
    List<GithubRepositoryResponseDto> getRepositoriesByUser(String userId);

    /** Retourne un dépôt par sa clé composite (userId, owner, name). */
    Optional<GithubRepositoryResponseDto> getRepository(String userId, String repoOwner, String repoName);

    /**
     * Retourne le token chiffré brut d'un dépôt privé.
     * Cet endpoint est UNIQUEMENT appelé par le service IA Python interne.
     */
    Optional<String> getEncryptedToken(String userId, String repoOwner, String repoName);

    /** Supprime un dépôt de la configuration de l'utilisateur. */
    void deleteRepository(String userId, String repoOwner, String repoName);
}
