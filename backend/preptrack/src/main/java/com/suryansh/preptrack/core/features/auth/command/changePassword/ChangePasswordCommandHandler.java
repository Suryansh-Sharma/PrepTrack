package com.suryansh.preptrack.core.features.auth.command.changePassword;

import com.suryansh.preptrack.core.exception.InvalidCredentialsException;
import com.suryansh.preptrack.core.features.auth.domain.AppUser;
import com.suryansh.preptrack.core.features.auth.domain.PasswordHistory;
import com.suryansh.preptrack.core.features.auth.domain.repository.PasswordHistoryRepository;
import com.suryansh.preptrack.core.features.auth.domain.repository.RefreshTokenRepository;
import com.suryansh.preptrack.core.security.CurrentUserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ChangePasswordCommandHandler {

    private final CurrentUserService currentUserService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordHistoryRepository passwordHistoryRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public void handle(ChangePasswordCommand command) {
        try {
            AppUser user = currentUserService.getUser();
            if (!passwordEncoder.matches(command.currentPassword(), user.getPasswordHash())) {
                throw new InvalidCredentialsException("Current password is incorrect");
            }
            Instant now = Instant.now();
            boolean passwordReused = passwordHistoryRepository.findTop5ByUserIdOrderByCreatedAtDesc(user.getId()).stream().anyMatch(history -> passwordEncoder.matches(command.newPassword(), history.getPasswordHash()));
            if (passwordReused) {
                throw new InvalidCredentialsException("You cannot reuse a previous password");
            }
            PasswordHistory history = PasswordHistory.builder().user(user).passwordHash(user.getPasswordHash()).createdAt(now).build();
            passwordHistoryRepository.save(history);
            user.setPasswordHash(passwordEncoder.encode(command.newPassword()));
            refreshTokenRepository.revokeAllByUserId(user.getId());

            log.info("Password changed successfully for userId={}", user.getId());
        } catch (InvalidCredentialsException e) {
            log.warn("Password change failed for authenticated user: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while changing password", e);
            throw e;
        }
    }
}