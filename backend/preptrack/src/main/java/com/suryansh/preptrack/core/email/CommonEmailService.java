package com.suryansh.preptrack.core.email;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class CommonEmailService {

    private final TemplateEngine templateEngine;

    public String createNotificationEmail(String title, String displayName, String message,
                                          String actionUrl, String actionText, String footerMessage) {
        Context context = new Context();
        context.setVariable("title", title);
        context.setVariable("displayName", displayName);
        context.setVariable("message", message);
        if (actionUrl != null) context.setVariable("actionUrl", actionUrl);
        if (actionText != null) context.setVariable("actionText", actionText);
        if (footerMessage != null) context.setVariable("footerMessage", footerMessage);

        return templateEngine.process("email/common-notification", context);
    }

    public String createLoginNotification(String displayName, String deviceDetails, String time) {
        String message = String.format("A new login to your PrepTrack account was detected from %s at %s.", deviceDetails, time);
        return createNotificationEmail("New Login Detected", displayName, message, null, null,
                "If this wasn't you, please reset your password immediately.");
    }

    public String createAccountVerifiedNotification(String displayName, String loginUrl) {
        String message = "Your PrepTrack account has been successfully verified. You can now access all features.";
        return createNotificationEmail("Account Verified Successfully", displayName, message, loginUrl, "Login Now", null);
    }

    public String createPasswordChangeNotification(String displayName) {
        String message = "Your PrepTrack password was changed successfully.";
        return createNotificationEmail("Password Changed", displayName, message, null, null,
                "If you did not perform this action, please contact support or reset your password immediately.");
    }

    public String createPasswordResetNotification(String displayName, String resetUrl) {
        String message = "We received a request to reset your PrepTrack password. Click the button below to set a new password.";
        return createNotificationEmail("Password Reset Request", displayName, message, resetUrl, "Reset Password",
                "If you did not request a password reset, you can safely ignore this email.");
    }
}
