package com.suryansh.preptrack.core.features.auth.command.resendVerification;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResendVerificationCommand(@NotBlank @Email String email) {
}
