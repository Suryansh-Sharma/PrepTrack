package com.suryansh.preptrack.core.features.auth.event;

public record PasswordResetSuccessEvent(
        String email,
        String displayName
) {
}
