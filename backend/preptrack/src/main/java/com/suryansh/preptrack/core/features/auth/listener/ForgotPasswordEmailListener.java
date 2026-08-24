package com.suryansh.preptrack.core.features.auth.listener;

import com.suryansh.preptrack.core.email.EmailSender;
import com.suryansh.preptrack.core.email.ForgotPasswordEmailService;
import com.suryansh.preptrack.core.features.auth.event.ForgotPasswordEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ForgotPasswordEmailListener {
    private static final Logger logger = LoggerFactory.getLogger(ForgotPasswordEmailListener.class.getName());
    private final ForgotPasswordEmailService forgotPasswordEmailService;
    private final EmailSender emailSender;

    @Async("emailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ForgotPasswordEvent event) {
        logger.info("Handling UserRegisteredEvent for email={}", event.email());
        try {
            String html = forgotPasswordEmailService.CreateEmail(event.email(), event.forgotPasswordUrl());
            emailSender.sendHtml(event.email(), "Reset your PrepTrack account password", html);
            logger.info("ForgotPassword email successfully dispatched for email={}", event.email());
        } catch (Exception e) {
            logger.error("Failed to send forgot-password email to {}", event.email(), e);
        }

    }
}
