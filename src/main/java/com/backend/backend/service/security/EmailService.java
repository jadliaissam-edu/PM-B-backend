package com.backend.backend.service.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import com.backend.backend.service.serviceInterface.IEmailService;

@Service
public class EmailService implements IEmailService {

    // Client SMTP fourni automatiquement par Spring Boot.
    private final JavaMailSender mailSender;

    // Adresse expeditrice lue depuis application.properties (spring.mail.username).
    private final String fromEmail;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${spring.mail.username}") String fromEmail
    ) {
        // On stocke le client SMTP injecte par Spring.
        this.mailSender = mailSender;
        // On stocke l'email expediteur configure dans les proprietes.
        this.fromEmail = fromEmail;
    }

    @Override
    public void sendOtpEmail(String toEmail, String otpCode, long otpDurationMinutes) {
        // On cree un email texte simple (sans HTML) pour rester minimal.
        SimpleMailMessage message = new SimpleMailMessage();
        // Adresse de l'expediteur.
        message.setFrom(fromEmail);
        // Adresse du destinataire.
        message.setTo(toEmail);
        // Sujet de l'email.
        message.setSubject("Votre code OTP de connexion");
        // Corps du message avec le code OTP et sa duree de validite.
        message.setText(
                "Bonjour,\n\n" +
                "Votre code OTP est : " + otpCode + "\n" +
                "Ce code expire dans " + otpDurationMinutes + " minutes.\n\n" +
                "Si vous n'etes pas a l'origine de cette tentative de connexion, ignorez cet email."
        );
        // Envoi effectif via le serveur SMTP configure.
        mailSender.send(message);
    }

    @Override
    public void sendWorkspaceInvitationEmail(
            String toEmail,
            String workspaceName,
            String inviterName,
            String invitationUrl,
            boolean hasAccount
    ) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Invitation workspace: " + workspaceName);

        String actionText = hasAccount
                ? "Vous etes deja inscrit. Ouvrez la page d'accueil invitation puis continuez avec votre session actuelle ou reconnectez-vous."
                : "Creez votre compte pour accepter l'invitation, puis connectez-vous normalement.";

        message.setText(
                "Bonjour,\n\n" +
                inviterName + " vous a invite a rejoindre le workspace: " + workspaceName + ".\n\n" +
                actionText + "\n\n" +
                "Lien invitation: " + invitationUrl + "\n\n" +
                "Si vous n'etes pas concerne, ignorez cet email."
        );

        mailSender.send(message);
    }
}