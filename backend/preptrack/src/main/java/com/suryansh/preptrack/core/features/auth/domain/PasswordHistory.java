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
@Table(name = "password_history")
public class PasswordHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String passwordHash;
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    private AppUser user;
}
