package com.suryansh.preptrack.core.features.auth.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "auth_login_attempt")
public class AuthLoginAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            foreignKey = @ForeignKey(
                    name = "fk_auth_login_attempt_user"
            )
    )
    private AppUser user;
    private String email;
    private boolean successful;
    private String ipAddress;
    private String userAgent;
    private Instant attemptedAt;
}
