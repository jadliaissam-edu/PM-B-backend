package com.backend.backend.dto.invitation;

import com.backend.backend.dao.enums.InvitationStatus;
import com.backend.backend.dao.enums.WorkspaceRole;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InvitationResponseDto {
    private String id;
    private String inviteeEmail;
    private WorkspaceRole role;
    private InvitationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;
    private String workspaceId;
    private String workspaceName;
    private String inviterName;
    private String message;
}
