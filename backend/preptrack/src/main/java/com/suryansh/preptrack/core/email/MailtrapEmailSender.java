package com.suryansh.preptrack.core.email;

import io.mailtrap.client.MailtrapClient;
import io.mailtrap.model.request.emails.Address;
import io.mailtrap.model.request.emails.MailtrapMail;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MailtrapEmailSender implements EmailSender {
    private static final Logger logger = LoggerFactory.getLogger(MailtrapEmailSender.class);
    private final MailtrapClient client;
    private final MailtrapProperties properties;

    @Override
    public void sendHtml(String to, String subject, String htmlBody) {
        logger.info("Sending email to {} with subject: {}", to, subject);
        MailtrapMail mail = MailtrapMail.builder()
                .from(
                        new Address(
                                properties.fromEmail(),
                                properties.fromName()
                        )
                )
                .to(List.of(new Address(to)))
                .subject(subject)
                .html(htmlBody)
                .build();
        try {
            client.send(mail);
            logger.info("Successfully sent email to {}", to);
        } catch (Exception e) {
            logger.error("Failed to send email to {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
