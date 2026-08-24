package com.suryansh.preptrack.core.features.auth.command.refreshToken;

import java.time.Instant;

public record RefreshTokenResponse(String token, Instant expiresAt) {
}
