package com.backend.backend.service.manager;

import com.backend.backend.dao.entities.User;
import com.backend.backend.dao.entities.Workspace;
import com.backend.backend.dao.entities.WorkspaceInvitation;
import com.backend.backend.dao.entities.WorkspaceMember;
import com.backend.backend.dao.enums.InvitationStatus;
import com.backend.backend.dao.enums.WorkspaceRole;
import com.backend.backend.dao.repositories.UserRepository;
import com.backend.backend.dao.repositories.WorkspaceInvitationRepository;
import com.backend.backend.dao.repositories.WorkspaceMemberRepository;
import com.backend.backend.dao.repositories.WorkspaceRepository;
import com.backend.backend.dto.invitation.InvitationResponseDto;
import com.backend.backend.dto.workspaceMember.InviteMemberRequestDto;
import com.backend.backend.dto.workspaceMember.RoleRequest;
import com.backend.backend.dto.workspaceMember.WorkspaceMemberRequestDto;
import com.backend.backend.dto.workspaceMember.WorkspaceMemberResponseDto;
import com.backend.backend.mapper.WorkspaceMemberMapper;
import com.backend.backend.service.serviceInterface.IAuthService;
import com.backend.backend.service.serviceInterface.IEmailService;
import com.backend.backend.service.serviceInterface.IWorkspaceMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkspaceMemberManager implements IWorkspaceMemberService {




    private final WorkspaceMemberMapper workspaceMemberMapper;
    private final WorkspaceMemberRepository workspaceMemberRepository;
        private final WorkspaceInvitationRepository workspaceInvitationRepository;
    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
        private final IAuthService authService;
        private final IEmailService emailService;

        @Value("${app.frontend.base-url:http://localhost:5173}")
        private String frontendBaseUrl;

        private String normalizedFrontendBaseUrl() {
                String value = frontendBaseUrl != null ? frontendBaseUrl.trim() : "";
                if (value.isEmpty()) {
                        return "http://localhost:5173";
                }
                return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
        }


    @Override
    public List<WorkspaceMemberResponseDto> getWorkspaceMembersByWorkspacId(String workspaceId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new RuntimeException("Workspace Introuvable"));

        List<WorkspaceMember> workspaceMembers = workspaceMemberRepository.findByWorkspace(workspace);

        return workspaceMembers.stream()
                .map(member -> {
                    WorkspaceMemberResponseDto dto = workspaceMemberMapper.toResponseDto(member);

                    User user = userRepository.findById(member.getUser().getId())
                            .orElseThrow(() -> new RuntimeException("User introuvable"));

                    dto.setUserId(user.getId());
                    dto.setUserName(user.getName());
                    dto.setUserEmail(user.getEmail());

                    return dto;
                })
                .toList();
    }

    @Override
    public List<WorkspaceMemberResponseDto> getWorkspaceByUserId(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User introuvable !") );

        List<WorkspaceMember> workspaceMembers = workspaceMemberRepository.findByUser(user);


        return workspaceMembers.stream()
                .map(member -> {
                    WorkspaceMemberResponseDto dto = workspaceMemberMapper.toResponseDto(member);
                    dto.setUserId(user.getId());
                    dto.setUserName(user.getName());
                    dto.setUserEmail(user.getEmail());
                    return dto;
                })
                .toList();
    }

    @Override
    public WorkspaceMemberResponseDto addWorkspaceMember(WorkspaceMemberRequestDto workspaceMemberRequestDto) {
        WorkspaceMember workspaceMember = workspaceMemberMapper.requesttoEntity(workspaceMemberRequestDto);

        User user = userRepository.findById(workspaceMemberRequestDto.getUserId())
                .orElseThrow(() -> new RuntimeException("User introuvable"));

        workspaceMember.setUser(user);

        workspaceMember.setWorkspace(
                workspaceRepository.findById(workspaceMemberRequestDto.getWorkspaceId())
                        .orElseThrow(() -> new RuntimeException("Workspace introuvable"))
        );

        workspaceMember.setJoinedAt(LocalDateTime.now());

        WorkspaceMember createdworkspaceMember = workspaceMemberRepository.save(workspaceMember);

        WorkspaceMemberResponseDto workspaceMemberResponseDto = workspaceMemberMapper.toResponseDto(createdworkspaceMember);

        workspaceMemberResponseDto.setUserId(user.getId());
        workspaceMemberResponseDto.setUserName(user.getName());
        workspaceMemberResponseDto.setUserEmail(user.getEmail());

        return workspaceMemberResponseDto;
    }

    @Override
        public InvitationResponseDto inviteByEmail(InviteMemberRequestDto dto) {
                if (dto == null || dto.getEmail() == null || dto.getEmail().isBlank()) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email d'invitation obligatoire.");
                }

                String inviteeEmail = dto.getEmail().trim().toLowerCase(Locale.ROOT);
                WorkspaceRole role = dto.getRole() != null ? dto.getRole() : WorkspaceRole.MEMBER;

        Workspace workspace = workspaceRepository.findById(dto.getWorkspaceId())
                .orElseThrow(() -> new RuntimeException("Workspace introuvable"));

                Optional<User> existingUser = userRepository.findByEmail(inviteeEmail);

                if (existingUser.isPresent() && workspaceMemberRepository.existsByUserAndWorkspace(existingUser.get(), workspace)) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "Cet utilisateur est deja membre du workspace.");
                }

                boolean pendingAlreadyExists = workspaceInvitationRepository.existsByInviteeEmailIgnoreCaseAndWorkspaceAndStatus(
                                inviteeEmail,
                                workspace,
                                InvitationStatus.PENDING
                );

                if (pendingAlreadyExists) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "Une invitation en attente existe deja pour cet email.");
                }

                WorkspaceInvitation invitation = new WorkspaceInvitation();
                invitation.setInviteeEmail(inviteeEmail);
                invitation.setWorkspace(workspace);
                invitation.setRole(role);
                invitation.setStatus(InvitationStatus.PENDING);
                invitation.setCreatedAt(LocalDateTime.now());

                User inviter;
                try {
                        inviter = authService.getCurrentUser();
                } catch (Exception ignored) {
                        inviter = null;
                }
                invitation.setInvitedBy(inviter);

                WorkspaceInvitation saved = workspaceInvitationRepository.save(invitation);

                boolean hasAccount = existingUser.isPresent();
                String inviteUrl = normalizedFrontendBaseUrl()
                        + "/invite?email=" + URLEncoder.encode(inviteeEmail, StandardCharsets.UTF_8)
                        + "&workspaceId=" + URLEncoder.encode(workspace.getId(), StandardCharsets.UTF_8)
                        + "&hasAccount=" + hasAccount;
                String inviterName = inviter != null ? inviter.getName() : "Un membre de votre equipe";

                emailService.sendWorkspaceInvitationEmail(
                                inviteeEmail,
                                workspace.getName(),
                                inviterName,
                                inviteUrl,
                                hasAccount
                );

                InvitationResponseDto response = new InvitationResponseDto();
                response.setId(saved.getId());
                response.setInviteeEmail(saved.getInviteeEmail());
                response.setRole(saved.getRole());
                response.setStatus(saved.getStatus());
                response.setCreatedAt(saved.getCreatedAt());
                response.setRespondedAt(saved.getRespondedAt());
                response.setWorkspaceId(workspace.getId());
                response.setWorkspaceName(workspace.getName());
                response.setInviterName(inviterName);
                response.setMessage("Invitation envoyee avec succes.");

        return response;
    }

    @Override
    public WorkspaceMemberResponseDto updateWorkspaceMemberRole(String id, RoleRequest role) {
        WorkspaceMember workspaceMember = workspaceMemberRepository.findById(id)
                .orElseThrow( () -> new RuntimeException("Memeber du workspace  introuvable") );

        workspaceMember.setRole(role.getRole());

        WorkspaceMember updatedWorkspaceMember = workspaceMemberRepository.save(workspaceMember);

        WorkspaceMemberResponseDto workspaceMemberResponseDto = workspaceMemberMapper.toResponseDto(updatedWorkspaceMember);

        return workspaceMemberResponseDto;
    }

    @Override
    public void deleteWorkspaceMember(String workspaceMemberId) {
        workspaceMemberRepository.deleteById(workspaceMemberId);
    }

    @Override
    public WorkspaceMemberResponseDto getWorkspaceMemberById(String id) {
        WorkspaceMember workspaceMember = workspaceMemberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Membre du workspace introuvable !"));

        WorkspaceMemberResponseDto workspaceMemberResponseDto = workspaceMemberMapper.toResponseDto(workspaceMember);

        return workspaceMemberResponseDto;
    }
}

