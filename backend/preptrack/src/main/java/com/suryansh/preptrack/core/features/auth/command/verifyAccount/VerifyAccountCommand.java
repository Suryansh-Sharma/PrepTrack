package com.suryansh.preptrack.core.features.auth.command.verifyAccount;

import jakarta.validation.constraints.NotBlank;

public record VerifyAccountCommand(@NotBlank String token) {
}
