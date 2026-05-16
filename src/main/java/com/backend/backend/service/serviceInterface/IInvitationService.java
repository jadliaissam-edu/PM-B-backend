package com.backend.backend.service.serviceInterface;

import com.backend.backend.dto.invitation.InvitationResponseDto;

import java.util.List;

public interface IInvitationService {
    List<InvitationResponseDto> getMyInvitations();
    InvitationResponseDto acceptInvitation(String invitationId);
    InvitationResponseDto declineInvitation(String invitationId);
}
