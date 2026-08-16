package com.suryansh.preptrack.core.features.auth.command.forgotPassword;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordCommand(@Email @NotBlank String email) {
}
