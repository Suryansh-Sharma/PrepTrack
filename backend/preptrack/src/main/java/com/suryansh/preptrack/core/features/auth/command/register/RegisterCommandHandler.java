package com.suryansh.preptrack.core.features.auth.command.register;

import com.suryansh.preptrack.core.exception.DuplicateResourceException;
import com.suryansh.preptrack.core.features.auth.domain.AppUser;
import com.suryansh.preptrack.core.features.auth.domain.repository.AppUserRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class RegisterCommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(RegisterCommandHandler.class);
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterCommandHandler(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public RegisterResponse handle(@Valid RegisterCommand command) {
        appUserRepository.findByEmail(command.email()).ifPresent(_ -> {
            throw new DuplicateResourceException("Email already exists.");
        });
        Instant now = Instant.now();
        AppUser newUser = AppUser.builder().email(command.email()).passwordHash(passwordEncoder.encode(command.password()))
                .displayName(command.displayName()).timezone(command.timezone()).plan(AppUser.Plan.FREE)
                .status(AppUser.Status.PENDING_VERIFICATION).createdAt(now).updatedAt(now).build();
        AppUser savedUser = appUserRepository.save(newUser);
        logger.info("Registered user with id={}", savedUser.getId());
        return new RegisterResponse(savedUser.getId(), savedUser.getEmail(), savedUser.getDisplayName(), savedUser.getPlan(), savedUser.getStatus(), savedUser.getCreatedAt());
    }

}
