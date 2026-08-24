package com.suryansh.preptrack.core.features.auth.command.resendVerification;

import com.suryansh.preptrack.core.exception.TooManyRequestsException;
import com.suryansh.preptrack.core.features.auth.domain.EmailVerificationToken;
import com.suryansh.preptrack.core.features.auth.domain.repository.AppUserRepository;
import com.suryansh.preptrack.core.features.auth.domain.repository.EmailVerificationRepository;
import com.suryansh.preptrack.core.features.auth.event.UserRegisteredEvent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ResendVerificationCommandHandler {
    private final EmailVerificationRepository emailVerificationRepository;
    private final AppUserRepository appUserRepository;
    private final ApplicationEventPublisher eventPublisher;
    @Value("${resend-cooldown-second}")
    private long resendCooldownSecond;
    @Value("${security.jwt.email-verification-time}")
    private Duration emailValidMinutes;
    @Value("${app.verification-url:http://localhost:4200/verify-email}")
    private String verificationUrlPrefix;

    public void handle(ResendVerificationCommand command) {
        var user = appUserRepository.findByEmail(command.email()).orElse(null);
        if (user == null || user.getEmailVerifiedAt() != null) {
            return;
        }
        validateResendCooldown(user.getId());

        var now = Instant.now();
        emailVerificationRepository.deleteExpiredTokens(
                user.getId(),
                now
        );
        String tokenId = UUID.randomUUID().toString();
        var token = EmailVerificationToken.builder()
                .id(tokenId)
                .user(user)
                .expiresAt(now.plus(emailValidMinutes))
                .createdAt(now)
                .build();
        emailVerificationRepository.save(token);

        String verificationUrl = verificationUrlPrefix + "?token=" + tokenId;
        eventPublisher.publishEvent(
                new UserRegisteredEvent(
                        user.getId(),
                        user.getEmail(),
                        user.getDisplayName(),
                        verificationUrl
                )
        );
    }

    public void validateResendCooldown(Integer userId) {
        var latestToken = emailVerificationRepository.findTopByUserIdOrderByCreatedAtDesc(userId);
        if (latestToken.isEmpty()) {
            return;
        }
        Instant now = Instant.now();
        Instant nextAllowedAt = latestToken.get().getCreatedAt().plusSeconds(resendCooldownSecond);

        if (now.isBefore(nextAllowedAt)) {
            throw new TooManyRequestsException("Please wait before requesting another email");
        }
    }
}
