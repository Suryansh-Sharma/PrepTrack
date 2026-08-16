package com.suryansh.preptrack.core.features.auth.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "password_reset_token")
public class PasswordResetToken {
    @Id
    private String id;
    private Instant expiresAt;
    private Instant createdAt;
    private Instant usedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    private AppUser user;
}
