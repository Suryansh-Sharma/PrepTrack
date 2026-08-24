package com.suryansh.preptrack.core.features.auth.command.register;

import com.suryansh.preptrack.core.exception.DuplicateResourceException;
import com.suryansh.preptrack.core.features.auth.domain.AppUser;
import com.suryansh.preptrack.core.features.auth.domain.EmailVerificationToken;
import com.suryansh.preptrack.core.features.auth.domain.repository.AppUserRepository;
import com.suryansh.preptrack.core.features.auth.domain.repository.EmailVerificationRepository;
import com.suryansh.preptrack.core.features.auth.event.UserRegisteredEvent;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class RegisterCommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(RegisterCommandHandler.class);

    private final AppUserRepository appUserRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${security.jwt.email-verification-time:24h}")
    private Duration emailVerificationExpiry;

    @Value("${app.verification-url:http://localhost:4200/verify-email}")
    private String verificationUrlPrefix;

    public RegisterCommandHandler(
            AppUserRepository appUserRepository,
            EmailVerificationRepository emailVerificationRepository,
            PasswordEncoder passwordEncoder,
            ApplicationEventPublisher eventPublisher
    ) {
        this.appUserRepository = appUserRepository;
        this.emailVerificationRepository = emailVerificationRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public RegisterResponse handle(RegisterCommand command) {
        appUserRepository.findByEmail(command.email()).ifPresent(existingUser -> {
            throw new DuplicateResourceException("Email already exists.");
        });

        Instant now = Instant.now();
        AppUser newUser = AppUser.builder()
                .email(command.email())
                .passwordHash(passwordEncoder.encode(command.password()))
                .displayName(command.displayName())
                .timezone(command.timezone())
                .plan(AppUser.Plan.FREE)
                .status(AppUser.Status.PENDING_VERIFICATION)
                .createdAt(now)
                .updatedAt(now)
                .build();

        AppUser savedUser = appUserRepository.save(newUser);
        logger.info("Registered user with id={}", savedUser.getId());

        // Generate email verification token
        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .id(token)
                .user(savedUser)
                .createdAt(now)
                .expiresAt(now.plus(emailVerificationExpiry))
                .build();

        emailVerificationRepository.save(verificationToken);
        logger.info("Saved email verification token for userId={}", savedUser.getId());

        String verificationUrl = verificationUrlPrefix + "?token=" + token;

        // Publish event to send verification email asynchronously
        eventPublisher.publishEvent(
                new UserRegisteredEvent(
                        savedUser.getId(),
                        savedUser.getEmail(),
                        savedUser.getDisplayName(),
                        verificationUrl
                )
        );

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getDisplayName(),
                savedUser.getPlan(),
                savedUser.getStatus(),
                savedUser.getCreatedAt()
        );
    }
}
