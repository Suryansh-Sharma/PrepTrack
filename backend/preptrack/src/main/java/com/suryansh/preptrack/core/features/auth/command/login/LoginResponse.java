package com.suryansh.preptrack.core.features.auth.command.login;

import java.time.Instant;

public record LoginResponse(Integer id, String email, String displayName, String timezone, Instant emailVerifiedAt,
                            String plan, String role,String status, Instant deletedAt, Instant createdAt, Instant updatedAt,
                            AuthenticationInfo authentication) {
    public record AuthenticationInfo(String accessToken, String tokenType, Long expiresIn, String refreshToken,
                                     Instant refreshTokenExpiresIn,
                                     Instant issuedAt, Instant expiresAt) {
    }
}
