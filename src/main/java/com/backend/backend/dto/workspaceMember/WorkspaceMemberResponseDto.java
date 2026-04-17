package com.backend.backend.dto.workspaceMember;


import com.backend.backend.dao.enums.WorkspaceRole;
import lombok.Data;

@Data
public class WorkspaceMemberResponseDto {
    private String id;
    private WorkspaceRole role;
    private String userName;
    private String workspaceId;
}
