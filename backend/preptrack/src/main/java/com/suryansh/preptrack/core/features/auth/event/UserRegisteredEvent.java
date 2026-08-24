package com.suryansh.preptrack.core.features.auth.event;


public record UserRegisteredEvent(
        Integer userId,
        String email,
        String displayName,
        String verificationUrl
) {
}