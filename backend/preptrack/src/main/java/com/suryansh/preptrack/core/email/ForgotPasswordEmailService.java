package com.suryansh.preptrack.core.email;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class ForgotPasswordEmailService {
    private final TemplateEngine templateEngine;

    public String CreateEmail(String displayName, String resetPasswordUrl) {
        Context context = new Context();

        context.setVariable("displayName", displayName);
        context.setVariable("resetPasswordUrl", resetPasswordUrl);

        return templateEngine.process("email/forgot-password", context);
    }
}
