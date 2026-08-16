package com.suryansh.preptrack.core.features.auth.command.register;

import com.suryansh.preptrack.core.features.auth.domain.AppUser;

import java.time.Instant;

public record RegisterResponse(
        Integer id,
        String email,
        String displayName,
        AppUser.Plan plan,
        AppUser.Status status,
        Instant createdAt
) {
}