package com.suryansh.preptrack.core.features.auth.command.register;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterCommand(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
        String password,

        @NotBlank(message = "Display name is required")
        @Size(min = 2, max = 100)
        String displayName,

        @NotBlank(message = "Timezone is required")
        String timezone
) {
}