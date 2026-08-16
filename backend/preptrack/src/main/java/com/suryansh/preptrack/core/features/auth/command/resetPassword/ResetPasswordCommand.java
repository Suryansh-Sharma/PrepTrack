package com.suryansh.preptrack.core.features.auth.command.resetPassword;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordCommand(@NotBlank String token, @NotBlank String newPassword) {
}
