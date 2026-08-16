package com.suryansh.preptrack.core.features.auth.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "email_verification_token")
public class EmailVerificationToken {
    @Id
    private String id;
    private Instant expiresAt;
    private Instant createdAt;
    private Instant usedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false,foreignKey = @ForeignKey(name = "fk_email_verification_token_user"))
    private AppUser user;
}
