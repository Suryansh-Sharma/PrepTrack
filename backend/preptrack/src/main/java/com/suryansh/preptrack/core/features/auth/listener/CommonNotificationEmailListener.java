package com.suryansh.preptrack.core.features.auth.listener;

import com.suryansh.preptrack.core.email.CommonEmailService;
import com.suryansh.preptrack.core.email.EmailSender;
import com.suryansh.preptrack.core.features.auth.event.AccountVerifiedEvent;
import com.suryansh.preptrack.core.features.auth.event.LoginSuccessEvent;
import com.suryansh.preptrack.core.features.auth.event.PasswordChangedEvent;
import com.suryansh.preptrack.core.features.auth.event.PasswordResetSuccessEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class CommonNotificationEmailListener {
    private static final Logger logger = LoggerFactory.getLogger(CommonNotificationEmailListener.class.getName());
    private final CommonEmailService commonEmailService;
    private final EmailSender emailSender;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Async("emailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLoginSuccessEvent(LoginSuccessEvent event) {
        logger.info("Handling LoginSuccessEvent for email={}", event.email());
        try {
            String html = commonEmailService.createLoginNotification(event.displayName(), event.deviceDetails(), event.time());
            emailSender.sendHtml(event.email(), "New Login Detected", html);
            logger.info("LoginSuccess email successfully dispatched for email={}", event.email());
        } catch (Exception e) {
            logger.error("Failed to send login success email to {}", event.email(), e);
        }
    }

    @Async("emailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAccountVerifiedEvent(AccountVerifiedEvent event) {
        logger.info("Handling AccountVerifiedEvent for email={}", event.email());
        try {
            String html = commonEmailService.createAccountVerifiedNotification(event.displayName(), frontendUrl + "/login");
            emailSender.sendHtml(event.email(), "Account Verified Successfully", html);
            logger.info("AccountVerified email successfully dispatched for email={}", event.email());
        } catch (Exception e) {
            logger.error("Failed to send account verified email to {}", event.email(), e);
        }
    }

    @Async("emailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePasswordChangedEvent(PasswordChangedEvent event) {
        logger.info("Handling PasswordChangedEvent for email={}", event.email());
        try {
            String html = commonEmailService.createPasswordChangeNotification(event.displayName());
            emailSender.sendHtml(event.email(), "Password Changed", html);
            logger.info("PasswordChanged email successfully dispatched for email={}", event.email());
        } catch (Exception e) {
            logger.error("Failed to send password changed email to {}", event.email(), e);
        }
    }

    @Async("emailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePasswordResetSuccessEvent(PasswordResetSuccessEvent event) {
        logger.info("Handling PasswordResetSuccessEvent for email={}", event.email());
        try {
            String html = commonEmailService.createPasswordChangeNotification(event.displayName());
            emailSender.sendHtml(event.email(), "Password Reset Successfully", html);
            logger.info("PasswordResetSuccess email successfully dispatched for email={}", event.email());
        } catch (Exception e) {
            logger.error("Failed to send password reset success email to {}", event.email(), e);
        }
    }
}
