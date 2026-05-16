package com.backend.backend.service.serviceInterface;


import com.backend.backend.dao.entities.Workspace;
import com.backend.backend.dao.entities.WorkspaceMember;
import com.backend.backend.dao.enums.WorkspaceRole;
import com.backend.backend.dto.invitation.InvitationResponseDto;
import com.backend.backend.dto.workspaceMember.RoleRequest;
import com.backend.backend.dto.workspaceMember.WorkspaceMemberRequestDto;
import com.backend.backend.dto.workspaceMember.InviteMemberRequestDto;
import com.backend.backend.dto.workspaceMember.WorkspaceMemberResponseDto;

import java.util.List;

public interface IWorkspaceMemberService {
    public WorkspaceMemberResponseDto addWorkspaceMember(WorkspaceMemberRequestDto WorkspaceMemberRequestDTO);
    public WorkspaceMemberResponseDto updateWorkspaceMemberRole(String id, RoleRequest role);
    public void deleteWorkspaceMember(String WorkspaceMemberId);
    public List<WorkspaceMemberResponseDto> getWorkspaceMembersByWorkspacId(String workspaceId);
    public WorkspaceMemberResponseDto getWorkspaceMemberById(String id);
    public List<WorkspaceMemberResponseDto> getWorkspaceByUserId(String id);
    public InvitationResponseDto inviteByEmail(InviteMemberRequestDto dto);
}
