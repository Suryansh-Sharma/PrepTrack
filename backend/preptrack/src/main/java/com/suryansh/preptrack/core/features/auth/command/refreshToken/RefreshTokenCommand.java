package com.suryansh.preptrack.core.features.auth.command.refreshToken;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenCommand(@NotBlank String refreshToken) {
}
