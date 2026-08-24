package com.suryansh.preptrack.core.features.auth.event;

public record LoginSuccessEvent(
        String email,
        String displayName,
        String deviceDetails,
        String time
) {
}
