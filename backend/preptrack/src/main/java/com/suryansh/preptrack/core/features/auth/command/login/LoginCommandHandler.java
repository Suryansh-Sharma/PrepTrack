package com.suryansh.preptrack.core.features.auth.command.login;

import com.suryansh.preptrack.core.exception.InvalidCredentialsException;
import com.suryansh.preptrack.core.features.auth.command.refreshToken.RefreshTokenCommandHandler;
import com.suryansh.preptrack.core.features.auth.command.refreshToken.RefreshTokenResponse;
import com.suryansh.preptrack.core.features.auth.domain.AppUser;
import com.suryansh.preptrack.core.features.auth.domain.AuthLoginAttempt;
import com.suryansh.preptrack.core.features.auth.domain.repository.AppUserRepository;
import com.suryansh.preptrack.core.features.auth.domain.repository.AuthLoginAttemptRepository;
import com.suryansh.preptrack.core.features.auth.event.LoginSuccessEvent;
import com.suryansh.preptrack.core.features.auth.security.UserPrincipal;
import com.suryansh.preptrack.core.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Service
@Transactional
public class LoginCommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(LoginCommandHandler.class);
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenCommandHandler refreshTokenCommandHandler;
    private final AuthLoginAttemptRepository authLoginAttemptRepository;
    private final ApplicationEventPublisher eventPublisher;
    @Value("${security.jwt.expiration-time}")
    private long accessTokenExpiration;

    public LoginCommandHandler(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder, JwtService jwtService, RefreshTokenCommandHandler refreshTokenCommandHandler, AuthLoginAttemptRepository authLoginAttemptRepository, ApplicationEventPublisher eventPublisher) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenCommandHandler = refreshTokenCommandHandler;
        this.authLoginAttemptRepository = authLoginAttemptRepository;
        this.eventPublisher = eventPublisher;
    }

    public LoginResponse handle(LoginCommand command, String ip, String device) {
        var userOptional = appUserRepository.findByEmail(command.email());
        if (userOptional.isEmpty()) {
            recordLoginAttempt(null, command.email(), false, ip, device);
            throw new InvalidCredentialsException("Invalid credentials");
        }
        var user = userOptional.get();

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        if (!passwordEncoder.matches(command.password(), user.getPasswordHash())) {
            recordLoginAttempt(user, command.email(), false, ip, device);
            handleFailedLogin(user);
            throw new InvalidCredentialsException("Invalid credentials");
        }

        recordLoginAttempt(user, command.email(), true, ip, device);
        // Reset failed attempts
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);

        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusSeconds(accessTokenExpiration);

        UserPrincipal userPrincipal = new UserPrincipal(user);
        String accessToken = jwtService.generateToken(userPrincipal);

        RefreshTokenResponse refreshTokenResponse = refreshTokenCommandHandler.create(user, device, ip);

        logger.info("User {} successfully logged in", userPrincipal.getId());

        String timeZoneStr = user.getTimezone() != null && !user.getTimezone().trim().isEmpty() ? user.getTimezone().trim() : "UTC";
        java.time.ZoneId zoneId;
        try {
            zoneId = java.time.ZoneId.of(timeZoneStr, java.time.ZoneId.SHORT_IDS);
        } catch (Exception e) {
            zoneId = java.time.ZoneId.of("UTC");
        }
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a z");
        String formattedTime = java.time.ZonedDateTime.now(zoneId).format(formatter);

        eventPublisher.publishEvent(new LoginSuccessEvent(
                user.getEmail(),
                user.getDisplayName(),
                device,
                formattedTime
        ));

        return new LoginResponse(userPrincipal.getId(), userPrincipal.getUsername(), userPrincipal.getDisplayName(), userPrincipal.getTimezone(), userPrincipal.getEmailVerifiedAt(), userPrincipal.getPlan(), userPrincipal.getStatus(), userPrincipal.getDeletedAt(), userPrincipal.getCreatedAt(), userPrincipal.getUpdatedAt(), new LoginResponse.AuthenticationInfo(accessToken, "Bearer", accessTokenExpiration, refreshTokenResponse.token(), refreshTokenResponse.expiresAt(), issuedAt, expiresAt));
    }

    private void recordLoginAttempt(AppUser user, String email, boolean status, String ip, String device) {
        AuthLoginAttempt attempt = new AuthLoginAttempt();

        attempt.setUser(user);
        attempt.setEmail(email);
        attempt.setSuccessful(status);
        attempt.setIpAddress(ip);
        attempt.setUserAgent(device);
        attempt.setAttemptedAt(Instant.now());

        authLoginAttemptRepository.save(attempt);
    }

    private void handleFailedLogin(AppUser user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);
        if (attempts >= 5) {
            user.setLockedUntil(Instant.now().plusSeconds(15 * 60));
        }
        appUserRepository.save(user);
    }
}
