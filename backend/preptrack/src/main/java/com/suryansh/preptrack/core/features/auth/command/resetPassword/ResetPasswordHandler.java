package com.suryansh.preptrack.core.features.auth.command.resetPassword;

import com.suryansh.preptrack.core.exception.InvalidCredentialsException;
import com.suryansh.preptrack.core.features.auth.domain.AppUser;
import com.suryansh.preptrack.core.features.auth.domain.PasswordHistory;
import com.suryansh.preptrack.core.features.auth.domain.PasswordResetToken;
import com.suryansh.preptrack.core.features.auth.domain.repository.PasswordHistoryRepository;
import com.suryansh.preptrack.core.features.auth.domain.repository.PasswordResetTokenRepository;
import com.suryansh.preptrack.core.features.auth.domain.repository.RefreshTokenRepository;
import com.suryansh.preptrack.core.features.auth.event.PasswordResetSuccessEvent;
import com.suryansh.preptrack.core.security.TokenService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Transactional
public class ResetPasswordHandler {
    private static final Logger logger = LoggerFactory.getLogger(ResetPasswordHandler.class);
    private final TokenService tokenService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordHistoryRepository passwordHistoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ResetPasswordHandler(TokenService tokenService, PasswordResetTokenRepository passwordResetTokenRepository, PasswordEncoder passwordEncoder, RefreshTokenRepository refreshTokenRepository, PasswordHistoryRepository passwordHistoryRepository, ApplicationEventPublisher eventPublisher) {
        this.tokenService = tokenService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordHistoryRepository = passwordHistoryRepository;
        this.eventPublisher = eventPublisher;
    }

    public void handle(ResetPasswordCommand command) {
        try {
            String tokenHash = tokenService.hashToken(command.token());
            PasswordResetToken resetToken = passwordResetTokenRepository.findById(tokenHash).orElseThrow(() -> new InvalidCredentialsException("Invalid or expired reset token"));
            if (resetToken.getUsedAt() != null) {
                throw new InvalidCredentialsException("Invalid or expired reset token");
            }
            Instant now = Instant.now();

            if (resetToken.getExpiresAt().isBefore(now)) {
                throw new InvalidCredentialsException("Invalid or expired reset token");
            }
            AppUser user = resetToken.getUser();
            PasswordHistory history = PasswordHistory.builder()
                    .user(user)
                    .passwordHash(user.getPasswordHash())
                    .createdAt(now)
                    .build();
            passwordHistoryRepository.save(history);

            user.setPasswordHash(passwordEncoder.encode(command.newPassword()));
            resetToken.setUsedAt(now);
            passwordResetTokenRepository.save(resetToken);
            refreshTokenRepository.revokeAllByUserId(user.getId());

            logger.info("Password reset successfully for userId={}", user.getId());
            eventPublisher.publishEvent(new PasswordResetSuccessEvent(user.getEmail(), user.getDisplayName()));
        } catch (InvalidCredentialsException e) {
            logger.warn("Password reset failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error while resetting password", e);
            throw e;
        }
    }
}
