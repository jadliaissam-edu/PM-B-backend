package com.backend.backend.service.manager;

import com.backend.backend.dao.entities.User;
import com.backend.backend.dao.repositories.UserRepository;
import com.backend.backend.dto.auth.AuthResponseDto;
import com.backend.backend.dto.auth.AuthUserDto;
import com.backend.backend.dto.auth.LoginRequestDto;
import com.backend.backend.dto.auth.LogoutRequestDto;
import com.backend.backend.dto.auth.MfaVerifyRequestDto;
import com.backend.backend.dto.auth.RefreshTokenRequestDto;
import com.backend.backend.dto.auth.RegisterRequestDto;
import com.backend.backend.dto.auth.TokenResponseDto;
import com.backend.backend.service.security.JwtService;
import com.backend.backend.service.serviceInterface.IAuthService;
import com.backend.backend.service.serviceInterface.IEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthManager implements IAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // Injection Spring via interface pour decoupler AuthManager de l'implementation concrete.
    @Autowired
    private IEmailService emailService;

    // Nombre de minutes pendant lesquelles le code OTP reste valide.
    @Value("${mfa.otp.duration-minutes:5}")
    private long mfaOtpDurationMinutes;

    // Nombre maximal de tentatives de saisie OTP avant blocage du challenge.
    @Value("${mfa.otp.max-attempts:5}")
    private int mfaOtpMaxAttempts;

    // Generateur aleatoire securise pour produire les OTP.
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    public AuthResponseDto register(RegisterRequestDto request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Requete invalide.");
        }

        String email = normalizeEmail(request.getEmail());
        String firstName = normalizeText(request.getFirstName());
        String lastName = normalizeText(request.getLastName());

        if (email == null || firstName == null || lastName == null || isBlank(request.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "firstName, lastName, email et password sont obligatoires.");
        }

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cet email existe deja.");
        }

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setName(firstName + " " + lastName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword().trim()));
        user.setRole(resolveRole(request.getRole()));
        // On active la MFA uniquement si le client l'a explicitement demandee.
        user.setMfaEnabled(Boolean.TRUE.equals(request.getMfaEnabled()));
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        return buildAuthSuccessResponse(savedUser);
    }

    @Override
    public User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email;

        if (principal instanceof String && !"anonymousUser".equals(principal)) {
            email = (String) principal;
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non authentifié");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur introuvable"));
        return user;
    }

    @Override
    public AuthResponseDto login(LoginRequestDto request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Requete invalide.");
        }

        String email = normalizeEmail(request.getEmail());
        String password = request.getPassword();

        if (email == null || isBlank(password)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email et password sont obligatoires.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou mot de passe incorrect."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou mot de passe incorrect.");
        }

        // Si la MFA est active, on ne renvoie pas encore les tokens finaux.
        if (Boolean.TRUE.equals(user.getMfaEnabled())) {
            return buildMfaChallengeResponse(user);
        }

        return buildAuthSuccessResponse(user);
    }

    @Override
    public AuthResponseDto verifyMfa(MfaVerifyRequestDto request) {
        // Validation de base du payload recu.
        if (request == null || isBlank(request.getChallengeToken()) || isBlank(request.getOtp())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "challengeToken et otp sont obligatoires.");
        }

        String challengeToken = request.getChallengeToken().trim();
        String otp = request.getOtp().trim();

        // Le token challenge doit etre valide, signe, non expire et du bon type.
        if (!jwtService.isValidMfaChallengeToken(challengeToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Challenge MFA invalide ou expire.");
        }

        // On retrouve l'utilisateur depuis le sujet du token challenge.
        String email = jwtService.extractSubject(challengeToken);
        String challengeId = jwtService.extractChallengeId(challengeToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur introuvable."));

        // Si la MFA n'est pas active, ce endpoint n'a pas de sens.
        if (!Boolean.TRUE.equals(user.getMfaEnabled())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "MFA non active pour cet utilisateur.");
        }

        // Le challenge doit correspondre exactement a celui stocke en base.
        if (isBlank(user.getMfaChallengeId()) || !user.getMfaChallengeId().equals(challengeId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Challenge MFA invalide.");
        }

        // Le challenge expire force un nouveau login complet.
        if (user.getMfaOtpExpiresAt() == null || LocalDateTime.now().isAfter(user.getMfaOtpExpiresAt())) {
            clearMfaState(user);
            userRepository.save(user);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Code OTP expire. Reconnectez-vous.");
        }

        int currentAttempts = user.getMfaOtpAttempts() == null ? 0 : user.getMfaOtpAttempts();

        // Si le nombre d'essais est depasse, on invalide le challenge.
        if (currentAttempts >= mfaOtpMaxAttempts) {
            clearMfaState(user);
            userRepository.save(user);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Trop de tentatives OTP. Reconnectez-vous.");
        }

        // Le code OTP saisi est compare au hash stocke en base.
        if (isBlank(user.getMfaOtpCodeHash()) || !passwordEncoder.matches(otp, user.getMfaOtpCodeHash())) {
            user.setMfaOtpAttempts(currentAttempts + 1);
            userRepository.save(user);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Code OTP incorrect.");
        }

        // OTP valide: on nettoie l'etat temporaire MFA avant de connecter l'utilisateur.
        clearMfaState(user);
        User savedUser = userRepository.save(user);

        return buildAuthSuccessResponse(savedUser);
    }

    @Override
    public TokenResponseDto refresh(RefreshTokenRequestDto request) {
        if (request == null || isBlank(request.getRefreshToken())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "refreshToken est obligatoire.");
        }

        String refreshToken = request.getRefreshToken().trim();

        if (!jwtService.isValidRefreshToken(refreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token invalide ou expire.");
        }

        String email = jwtService.extractSubject(refreshToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur introuvable."));

        return new TokenResponseDto(
                jwtService.generateAccessToken(user),
                jwtService.generateRefreshToken(user)
        );
    }

    @Override
    public void logout(LogoutRequestDto request) {
        // JWT stateless: pas de session serveur a detruire.
        // On valide simplement le format du refreshToken s'il est fourni.
        if (request == null || isBlank(request.getRefreshToken())) {
            return;
        }

        String refreshToken = request.getRefreshToken().trim();
        jwtService.isValidRefreshToken(refreshToken);
    }

    private AuthResponseDto buildAuthSuccessResponse(User user) {
        // Tokens finaux utilises pour les appels API metier.
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        AuthUserDto authUserDto = new AuthUserDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole()
        );

        AuthResponseDto response = new AuthResponseDto();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setUser(authUserDto);
        response.setMfaRequired(false);
        response.setChallengeToken(null);
        response.setMfaExpiresInSeconds(null);
        response.setMfaDestination(null);
        return response;
    }

    private AuthResponseDto buildMfaChallengeResponse(User user) {
        // On genere un code OTP a 6 chiffres.
        String otpCode = generateOtpCode();

        // On cree un identifiant unique de challenge pour eviter les replays.
        String challengeId = UUID.randomUUID().toString();

        // Date limite de validite du OTP/challenge.
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(mfaOtpDurationMinutes);

        // Stockage de l'etat MFA temporaire en base.
        user.setMfaChallengeId(challengeId);
        user.setMfaOtpCodeHash(passwordEncoder.encode(otpCode));
        user.setMfaOtpExpiresAt(expiresAt);
        user.setMfaOtpAttempts(0);
        userRepository.save(user);

        // Token court qui prouve que le facteur 1 (mot de passe) est deja valide.
        String challengeToken = jwtService.generateMfaChallengeToken(
                user,
                challengeId,
                mfaOtpDurationMinutes * 60_000
        );

        // On envoie le OTP par email, comme dans le pattern Presence (service SMTP simple).
        try {
            emailService.sendOtpEmail(user.getEmail(), otpCode, mfaOtpDurationMinutes);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Impossible d'envoyer le code OTP par email.");
        }

        AuthResponseDto response = new AuthResponseDto();
        response.setAccessToken(null);
        response.setRefreshToken(null);
        response.setUser(null);
        response.setMfaRequired(true);
        response.setChallengeToken(challengeToken);
        response.setMfaExpiresInSeconds((int) (mfaOtpDurationMinutes * 60));
        response.setMfaDestination(maskEmail(user.getEmail()));
        return response;
    }

    private void clearMfaState(User user) {
        // Nettoyage complet pour invalider le challenge/OTP courant.
        user.setMfaChallengeId(null);
        user.setMfaOtpCodeHash(null);
        user.setMfaOtpExpiresAt(null);
        user.setMfaOtpAttempts(null);
    }

    private String generateOtpCode() {
        // Valeur entre 000000 et 999999 formatee sur 6 chiffres.
        int value = SECURE_RANDOM.nextInt(1_000_000);
        return String.format("%06d", value);
    }

    private String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return "***";
        }

        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return "***";
        }

        String firstChar = email.substring(0, 1);
        String domain = email.substring(atIndex);
        return firstChar + "***" + domain;
    }

    private String resolveRole(String role) {
        if (role == null || role.isBlank()) {
            return "USER";
        }
        return role.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
