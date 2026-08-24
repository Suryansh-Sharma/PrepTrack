package com.suryansh.preptrack.core.features.auth.command.forgotPassword;

import com.suryansh.preptrack.core.exception.TooManyRequestsException;
import com.suryansh.preptrack.core.features.auth.domain.PasswordResetToken;
import com.suryansh.preptrack.core.features.auth.domain.repository.AppUserRepository;
import com.suryansh.preptrack.core.features.auth.domain.repository.PasswordResetTokenRepository;
import com.suryansh.preptrack.core.features.auth.event.ForgotPasswordEvent;
import com.suryansh.preptrack.core.security.TokenService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@Transactional
public class ForgotPasswordCommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(ForgotPasswordCommandHandler.class);
    private final AppUserRepository appUserRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final TokenService tokenService;
    private final ApplicationEventPublisher eventPublisher;
    @Value("${resend-cooldown-second}")
    private long Resend_Cooldown_Second;
    @Value("${security.jwt.email-verification-time}")
    private Duration emailValidMinutes;
    @Value("${app.forgotpassword-url}")
    private String forgotPasswordUrl;

    public ForgotPasswordCommandHandler(AppUserRepository appUserRepository, PasswordResetTokenRepository passwordResetTokenRepository, TokenService tokenService, ApplicationEventPublisher eventPublisher) {
        this.appUserRepository = appUserRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.tokenService = tokenService;
        this.eventPublisher = eventPublisher;
    }

    public void handle(ForgotPasswordCommand command) {
        var user = appUserRepository.findByEmail(command.email()).orElse(null);
        if (user == null) {
            return;
        }
        CheckResendCoolDown(user.getId());
        String rawToken = tokenService.generateToken();
        String hashToken = tokenService.hashToken(rawToken);
        logger.info("Raw token {} ", rawToken);
        var now = Instant.now();

        passwordResetTokenRepository.deleteExpiredTokenForUser(user.getId(), now);

        var entity = PasswordResetToken.builder()
                .id(hashToken)
                .expiresAt(now.plus(emailValidMinutes))
                .createdAt(now)
                .user(user)
                .build();
        passwordResetTokenRepository.save(entity);

        String verificationUrl = forgotPasswordUrl + "?token=" + rawToken;
        eventPublisher.publishEvent(
                new ForgotPasswordEvent(
                        user.getEmail(),
                        user.getDisplayName(),
                        verificationUrl
                )
        );
    }

    private void CheckResendCoolDown(Integer userId) {
        var latestToken = passwordResetTokenRepository.findTopByUserIdOrderByCreatedAtDesc(userId);
        if (latestToken.isEmpty()) {
            return;
        }
        Instant now = Instant.now();
        Instant nextAllowedAt = latestToken.get().getCreatedAt().plusSeconds(Resend_Cooldown_Second);

        if (now.isBefore(nextAllowedAt)) {
            throw new TooManyRequestsException("Please wait before requesting another email");
        }
    }
}
