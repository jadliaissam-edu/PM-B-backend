package com.backend.backend.dto.workspace;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import com.backend.backend.dto.space.SpaceResponseDto;
import com.backend.backend.dto.workspaceMember.WorkspaceMemberRequestDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceDto {
    private String id;
    private String name;
    private String slug;

    private String ownerId;
    private String ownerName;

    private LocalDateTime createdAt;

    private List<WorkspaceMemberRequestDto> workspaceMembers;
    private List<SpaceResponseDto> spaces;

}
