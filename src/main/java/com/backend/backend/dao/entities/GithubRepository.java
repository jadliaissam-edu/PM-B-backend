package com.backend.backend.dao.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "github_repository",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_user_owner_repo",
        columnNames = {"user_id", "repo_owner", "repo_name"}
    )
)
public class GithubRepository {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(nullable = false, updatable = false)
    private String id;

    /**
     * Identifiant de l'utilisateur propriétaire de ce dépôt configuré.
     * Correspond à l'ID de l'entité User.
     */
    @Column(name = "user_id", nullable = false)
    private String userId;

    /** Login GitHub du propriétaire du dépôt (ex: "facebook"). */
    @Column(name = "repo_owner", nullable = false)
    private String repoOwner;

    /** Nom du dépôt GitHub (ex: "react"). */
    @Column(name = "repo_name", nullable = false)
    private String repoName;

    /** Branche cible pour l'analyse (ex: "main", "develop"). */
    @Column(name = "branch", nullable = false)
    private String branch;

    /** Indique si le dépôt est privé. */
    @Column(name = "is_private", nullable = false)
    private boolean isPrivate;

    /**
     * Personal Access Token GitHub chiffré avec Fernet (AES-128-CBC + HMAC-SHA256).
     * Null pour les dépôts publics. Ne jamais stocker en clair.
     */
    @Column(name = "github_token_encrypted", columnDefinition = "TEXT")
    private String githubTokenEncrypted;

    /** Date d'enregistrement du dépôt. */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** Date de dernière mise à jour (upsert). */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
