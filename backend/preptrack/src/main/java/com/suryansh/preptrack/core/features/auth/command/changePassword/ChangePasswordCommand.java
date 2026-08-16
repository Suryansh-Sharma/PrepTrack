package com.suryansh.preptrack.core.features.auth.command.changePassword;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordCommand(@NotBlank String currentPassword,
                                    @NotBlank @Size(min = 8, max = 100) String newPassword) {
}
