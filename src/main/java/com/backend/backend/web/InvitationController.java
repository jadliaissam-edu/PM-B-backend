package com.backend.backend.web;

import com.backend.backend.dto.invitation.InvitationResponseDto;
import com.backend.backend.service.serviceInterface.IInvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final IInvitationService invitationService;

    @GetMapping("/my")
    public List<InvitationResponseDto> getMyInvitations() {
        return invitationService.getMyInvitations();
    }

    @PostMapping("/{id}/accept")
    public InvitationResponseDto acceptInvitation(@PathVariable("id") String invitationId) {
        return invitationService.acceptInvitation(invitationId);
    }

    @PostMapping("/{id}/decline")
    public InvitationResponseDto declineInvitation(@PathVariable("id") String invitationId) {
        return invitationService.declineInvitation(invitationId);
    }
}
