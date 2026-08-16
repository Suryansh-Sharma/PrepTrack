package com.suryansh.preptrack.core.features.auth.query.me;

import com.suryansh.preptrack.core.features.auth.domain.AppUser;
import com.suryansh.preptrack.core.security.CurrentUserService;
import org.springframework.stereotype.Service;

@Service
public class MeQueryHandler {
    private final CurrentUserService currentUserService;

    public MeQueryHandler(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    public MeResponseDto handle() {
        AppUser user = currentUserService.getUser();
        return new MeResponseDto(user.getId(),user.getEmail(),user.getDisplayName(),user.getTimezone(),user.getEmailVerifiedAt(),user.getPlan(),
                user.getStatus(),user.getDeletedAt(), user.getFailedLoginAttempts(), user.getLockedUntil(),user.getCreatedAt(),user.getUpdatedAt());
    }
}
