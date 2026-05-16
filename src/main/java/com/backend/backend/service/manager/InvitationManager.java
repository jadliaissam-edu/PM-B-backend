package com.backend.backend.service.manager;

import com.backend.backend.dao.entities.User;
import com.backend.backend.dao.entities.WorkspaceInvitation;
import com.backend.backend.dao.entities.WorkspaceMember;
import com.backend.backend.dao.enums.InvitationStatus;
import com.backend.backend.dao.repositories.WorkspaceInvitationRepository;
import com.backend.backend.dao.repositories.WorkspaceMemberRepository;
import com.backend.backend.dto.invitation.InvitationResponseDto;
import com.backend.backend.service.serviceInterface.IAuthService;
import com.backend.backend.service.serviceInterface.IInvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class InvitationManager implements IInvitationService {

    private final IAuthService authService;
    private final WorkspaceInvitationRepository workspaceInvitationRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;

    @Override
    public List<InvitationResponseDto> getMyInvitations() {
        User currentUser = authService.getCurrentUser();
        String email = currentUser.getEmail().toLowerCase(Locale.ROOT);

        return workspaceInvitationRepository.findByInviteeEmailOrderByCreatedAtDesc(email)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public InvitationResponseDto acceptInvitation(String invitationId) {
        User currentUser = authService.getCurrentUser();
        WorkspaceInvitation invitation = getOwnedInvitationOrThrow(invitationId, currentUser.getEmail());

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette invitation n'est plus en attente.");
        }

        if (!workspaceMemberRepository.existsByUserAndWorkspace(currentUser, invitation.getWorkspace())) {
            WorkspaceMember member = new WorkspaceMember();
            member.setUser(currentUser);
            member.setWorkspace(invitation.getWorkspace());
            member.setRole(invitation.getRole());
            member.setJoinedAt(LocalDateTime.now());
            workspaceMemberRepository.save(member);
        }

        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setRespondedAt(LocalDateTime.now());
        WorkspaceInvitation saved = workspaceInvitationRepository.save(invitation);

        InvitationResponseDto dto = toDto(saved);
        dto.setMessage("Invitation acceptee.");
        return dto;
    }

    @Override
    public InvitationResponseDto declineInvitation(String invitationId) {
        User currentUser = authService.getCurrentUser();
        WorkspaceInvitation invitation = getOwnedInvitationOrThrow(invitationId, currentUser.getEmail());

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette invitation n'est plus en attente.");
        }

        invitation.setStatus(InvitationStatus.DECLINED);
        invitation.setRespondedAt(LocalDateTime.now());
        WorkspaceInvitation saved = workspaceInvitationRepository.save(invitation);

        InvitationResponseDto dto = toDto(saved);
        dto.setMessage("Invitation refusee.");
        return dto;
    }

    private WorkspaceInvitation getOwnedInvitationOrThrow(String invitationId, String userEmail) {
        WorkspaceInvitation invitation = workspaceInvitationRepository.findById(invitationId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation introuvable."));

        String email = userEmail == null ? "" : userEmail.toLowerCase(Locale.ROOT);
        if (!invitation.getInviteeEmail().equalsIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acces refuse a cette invitation.");
        }
        return invitation;
    }

    private InvitationResponseDto toDto(WorkspaceInvitation invitation) {
        InvitationResponseDto dto = new InvitationResponseDto();
        dto.setId(invitation.getId());
        dto.setInviteeEmail(invitation.getInviteeEmail());
        dto.setRole(invitation.getRole());
        dto.setStatus(invitation.getStatus());
        dto.setCreatedAt(invitation.getCreatedAt());
        dto.setRespondedAt(invitation.getRespondedAt());
        dto.setWorkspaceId(invitation.getWorkspace().getId());
        dto.setWorkspaceName(invitation.getWorkspace().getName());
        dto.setInviterName(invitation.getInvitedBy() != null ? invitation.getInvitedBy().getName() : "Equipe");
        return dto;
    }
}
