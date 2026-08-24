package com.suryansh.preptrack.core.features.auth.event;

public record ForgotPasswordEvent(String email, String displayName, String forgotPasswordUrl) {
}
