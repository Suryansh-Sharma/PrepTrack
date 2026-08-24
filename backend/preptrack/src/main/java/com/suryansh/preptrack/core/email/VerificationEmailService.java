package com.suryansh.preptrack.core.email;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class VerificationEmailService {

    private final TemplateEngine templateEngine;

    public String createEmail(String displayName, String verificationUrl) {
        Context context = new Context();

        context.setVariable("displayName", displayName);
        context.setVariable("verificationUrl", verificationUrl);

        return templateEngine.process("email/verification", context);
    }
}
