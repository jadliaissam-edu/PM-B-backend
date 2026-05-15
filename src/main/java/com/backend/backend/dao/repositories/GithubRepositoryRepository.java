package com.backend.backend.dao.repositories;

import com.backend.backend.dao.entities.GithubRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GithubRepositoryRepository extends JpaRepository<GithubRepository, String> {

    /** Tous les dépôts configurés pour un utilisateur. */
    List<GithubRepository> findAllByUserId(String userId);

    /** Recherche exacte par (userId, owner, repoName) — correspond à la contrainte unique. */
    Optional<GithubRepository> findByUserIdAndRepoOwnerAndRepoName(
        String userId, String repoOwner, String repoName
    );

    /** Vérifie l'existence d'un dépôt pour cet utilisateur. */
    boolean existsByUserIdAndRepoOwnerAndRepoName(
        String userId, String repoOwner, String repoName
    );

    /** Suppression directe par clé composite. */
    void deleteByUserIdAndRepoOwnerAndRepoName(
        String userId, String repoOwner, String repoName
    );
}
