package com.suryansh.preptrack.core.email;

public interface EmailSender {
    void sendHtml(
            String to,
            String subject,
            String htmlBody
    );
}
