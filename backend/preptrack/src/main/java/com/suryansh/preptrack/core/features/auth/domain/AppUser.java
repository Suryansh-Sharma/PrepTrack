package com.suryansh.preptrack.core.features.auth.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "app_user")
@Entity
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "app_user_seq")
    @SequenceGenerator(name = "app_user_seq", sequenceName = "app_user_seq")
    private Integer id;
    private String email;
    private String passwordHash;
    private String displayName;
    private String timezone;
    private Instant emailVerifiedAt;
    @Enumerated(EnumType.STRING)
    private Plan plan;
    @Enumerated(EnumType.STRING)
    private Status status;
    private Instant deletedAt;
    private int failedLoginAttempts;
    private Instant lockedUntil;
    private Instant createdAt;
    private Instant updatedAt;
@Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RefreshToken> refreshTokens = new ArrayList<>();
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmailVerificationToken> emailVerificationTokens = new ArrayList<>();
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PasswordResetToken> passwordResetTokens = new ArrayList<>();
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PasswordHistory> passwordHistories = new ArrayList<>();

    public enum Status {
        PENDING_VERIFICATION, ACTIVE, LOCKED, DELETED
    }

    public enum Plan {FREE,PRO,PREMIUM}
    public enum Role {ADMIN,MANAGER,USER}
}
