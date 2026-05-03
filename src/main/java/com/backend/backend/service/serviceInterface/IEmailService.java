package com.backend.backend.service.serviceInterface;

public interface IEmailService {

    void sendOtpEmail(String toEmail, String otpCode, long otpDurationMinutes);

    void sendWorkspaceInvitationEmail(
            String toEmail,
            String workspaceName,
            String inviterName,
            String invitationUrl,
            boolean hasAccount
    );
}