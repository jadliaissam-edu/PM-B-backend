package com.backend.backend.dao.repositories;

import com.backend.backend.dao.entities.Workspace;
import com.backend.backend.dao.entities.WorkspaceInvitation;
import com.backend.backend.dao.enums.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkspaceInvitationRepository extends JpaRepository<WorkspaceInvitation, String> {

    List<WorkspaceInvitation> findByInviteeEmailOrderByCreatedAtDesc(String inviteeEmail);

    boolean existsByInviteeEmailIgnoreCaseAndWorkspaceAndStatus(
            String inviteeEmail,
            Workspace workspace,
            InvitationStatus status
    );
}
