package com.backend.backend.dao.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
// import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "app_users")
public class User {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(nullable = false, updatable = false)
    private String id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;

    // Active ou desactive la MFA pour l'utilisateur.
    @Column(nullable = false, columnDefinition = "boolean default false")
    private Boolean mfaEnabled = false;

    // Hash BCrypt du code OTP temporaire.
    private String mfaOtpCodeHash;

    // Date d'expiration du code OTP.
    private LocalDateTime mfaOtpExpiresAt;

    // Identifiant unique du challenge MFA en cours.
    private String mfaChallengeId;

    // Nombre de tentatives OTP ratees pour le challenge en cours.
    private Integer mfaOtpAttempts;

    private String avatarUrl;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user")
    private List<WorkspaceMember> workspaceMembers;


    @OneToMany(mappedBy = "assignee")
    private List<Task> assignedTasks;

    @OneToMany(mappedBy = "author")
    private List<Comment> comments;

    @OneToMany(mappedBy = "user")
    private List<Notification> notifications;

    @OneToMany(mappedBy = "assignee")
    private List<Subtask> assignedSubtasks;
}
