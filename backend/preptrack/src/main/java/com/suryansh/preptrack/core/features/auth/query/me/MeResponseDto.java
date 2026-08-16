package com.suryansh.preptrack.core.features.auth.query.me;

import com.suryansh.preptrack.core.features.auth.domain.AppUser;

import java.time.Instant;

public record MeResponseDto(Integer id,
                            String email,
                            String displayName,
                            String timezone,
                            Instant emailVerifiedAt,
                            AppUser.Plan plan,
                            AppUser.Status status,
                            Instant deletedAt,
                            int failedLoginAttempts,
                            Instant lockedUntil,
                            Instant createdAt,
                            Instant updatedAt) {
}
