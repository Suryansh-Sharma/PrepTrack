package com.suryansh.preptrack.core.features.auth.command.verifyAccount;

import com.suryansh.preptrack.core.exception.InvalidCredentialsException;
import com.suryansh.preptrack.core.features.auth.domain.AppUser;
import com.suryansh.preptrack.core.features.auth.domain.repository.EmailVerificationRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Transactional
public class VerifyAccountCommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(VerifyAccountCommandHandler.class);
    private final EmailVerificationRepository emailVerificationRepository;

    public VerifyAccountCommandHandler(EmailVerificationRepository emailVerificationRepository) {
        this.emailVerificationRepository = emailVerificationRepository;
    }

    public void handle(@Valid VerifyAccountCommand command) {
        try {
            var emailToken = emailVerificationRepository.findById(command.token()).orElseThrow(() -> new InvalidCredentialsException("Invalid or expired token"));
            if (emailToken.getUsedAt() != null) {
                throw new InvalidCredentialsException("Verification token has already been used");
            }

            Instant now = Instant.now();
            if (emailToken.getExpiresAt().isBefore(now)) {
                throw new InvalidCredentialsException("Verification token has expired");
            }

            AppUser user = emailToken.getUser();
            if (user.getEmailVerifiedAt() != null) {
                logger.info("Email already verified for userId={}", user.getId());
                emailToken.setUsedAt(now);
                emailVerificationRepository.save(emailToken);
                return;
            }

            user.setEmailVerifiedAt(now);
            user.setStatus(AppUser.Status.ACTIVE);
            emailToken.setUsedAt(now);
            emailVerificationRepository.save(emailToken);
            logger.info("Email verified successfully for userId={}", user.getId());
        } catch (InvalidCredentialsException e) {
            logger.warn("Email verification failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error while verifying email", e);
            throw e;
        }
    }
}
