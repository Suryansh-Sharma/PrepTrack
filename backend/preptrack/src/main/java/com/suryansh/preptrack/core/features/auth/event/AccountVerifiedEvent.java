package com.suryansh.preptrack.core.features.auth.event;

public record AccountVerifiedEvent(
        String email,
        String displayName
) {
}
