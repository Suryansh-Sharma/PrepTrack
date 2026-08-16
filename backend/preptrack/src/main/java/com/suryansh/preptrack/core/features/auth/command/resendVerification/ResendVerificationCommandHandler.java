package com.suryansh.preptrack.core.features.auth.command.resendVerification;

import com.suryansh.preptrack.core.exception.TooManyRequestsException;
import com.suryansh.preptrack.core.features.auth.domain.EmailVerificationToken;
import com.suryansh.preptrack.core.features.auth.domain.repository.AppUserRepository;
import com.suryansh.preptrack.core.features.auth.domain.repository.EmailVerificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ResendVerificationCommandHandler {
    @Value("${resend-cooldown-second}")
    private long Resend_Cooldown_Second;
    @Value("${security.jwt.email-verification-time}")
    private Duration emailValidMinutes;

    private final EmailVerificationRepository emailVerificationRepository;
    private final AppUserRepository appUserRepository;

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
        var token = EmailVerificationToken.builder()
                .id(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(now.plus(emailValidMinutes))
                .createdAt(now)
                .build();
        emailVerificationRepository.save(token);
        // Will send email in future here.
    }

    public void validateResendCooldown(Integer userId){
        var latestToken = emailVerificationRepository.findTopByUserIdOrderByCreatedAtDesc(userId);
        if(latestToken.isEmpty()){
            return;
        }
        Instant now = Instant.now();
        Instant nextAllowedAt = latestToken.get().getCreatedAt().plusSeconds(Resend_Cooldown_Second);

        if(now.isBefore(nextAllowedAt)){
            throw new TooManyRequestsException("Please wait before requesting another email");
        }

    }
}
