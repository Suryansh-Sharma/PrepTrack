package com.suryansh.preptrack.core.features.auth.query.session;

import java.time.Instant;

public record SessionInfoDto(String id, Instant expiresAt, Instant createdAt,
                             Instant lastUsedAt, String deviceInfo, String ipAddress) {
}
