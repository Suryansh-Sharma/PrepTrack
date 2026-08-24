package com.suryansh.preptrack.core.features.auth.event;

public record PasswordChangedEvent(
        String email,
        String displayName
) {
}
