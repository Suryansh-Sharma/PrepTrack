package com.suryansh.preptrack.core.features.auth.listener;

import com.suryansh.preptrack.core.email.EmailSender;
import com.suryansh.preptrack.core.email.VerificationEmailService;
import com.suryansh.preptrack.core.features.auth.event.UserRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class UserRegisteredEmailListener {
    private static final Logger logger = LoggerFactory.getLogger(UserRegisteredEmailListener.class);
    private final VerificationEmailService verificationEmailService;
    private final EmailSender emailSender;

    public UserRegisteredEmailListener(VerificationEmailService verificationEmailService, EmailSender emailSender) {
        this.verificationEmailService = verificationEmailService;
        this.emailSender = emailSender;
    }

    @Async("emailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(UserRegisteredEvent event) {
        logger.info("Handling UserRegisteredEvent for email={}", event.email());
        try {
            String html = verificationEmailService.createEmail(
                    event.displayName(),
                    event.verificationUrl()
            );

            emailSender.sendHtml(
                    event.email(),
                    "Verify your PrepTrack account",
                    html
            );
            logger.info("Verification email successfully dispatched for email={}", event.email());
        } catch (Exception e) {
            logger.error("Failed to send verification email to {}", event.email(), e);
        }
    }
}