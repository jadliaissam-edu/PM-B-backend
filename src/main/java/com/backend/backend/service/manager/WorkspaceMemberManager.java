package com.backend.backend.service.manager;

import com.backend.backend.dao.entities.User;
import com.backend.backend.dao.entities.Workspace;
import com.backend.backend.dao.entities.WorkspaceMember;
import com.backend.backend.dao.enums.WorkspaceRole;
import com.backend.backend.dao.repositories.UserRepository;
import com.backend.backend.dao.repositories.WorkspaceMemberRepository;
import com.backend.backend.dao.repositories.WorkspaceRepository;
import com.backend.backend.dto.workspaceMember.InviteMemberRequestDto;
import com.backend.backend.dto.workspaceMember.RoleRequest;
import com.backend.backend.dto.workspaceMember.WorkspaceMemberRequestDto;
import com.backend.backend.dto.workspaceMember.WorkspaceMemberResponseDto;
import com.backend.backend.mapper.WorkspaceMapper;
import com.backend.backend.mapper.WorkspaceMemberMapper;
import com.backend.backend.service.serviceInterface.IWorkspaceMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkspaceMemberManager implements IWorkspaceMemberService {




    private final WorkspaceMemberMapper workspaceMemberMapper;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;


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
    public WorkspaceMemberResponseDto inviteByEmail(InviteMemberRequestDto dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("No account found with email: " + dto.getEmail()));

        Workspace workspace = workspaceRepository.findById(dto.getWorkspaceId())
                .orElseThrow(() -> new RuntimeException("Workspace introuvable"));

        WorkspaceMember member = new WorkspaceMember();
        member.setUser(user);
        member.setWorkspace(workspace);
        member.setRole(dto.getRole() != null ? dto.getRole() : WorkspaceRole.MEMBER);
        member.setJoinedAt(LocalDateTime.now());

        WorkspaceMember saved = workspaceMemberRepository.save(member);

        WorkspaceMemberResponseDto response = workspaceMemberMapper.toResponseDto(saved);
        response.setUserId(user.getId());
        response.setUserName(user.getName());
        response.setUserEmail(user.getEmail());

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

