package com.suryansh.preptrack.core.email;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mailtrap")
public record MailtrapProperties(
        String token,
        String fromEmail,
        String fromName
) {
}
