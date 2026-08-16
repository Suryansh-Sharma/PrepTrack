package com.suryansh.preptrack.core.features.auth.domain.repository;

import com.suryansh.preptrack.core.features.auth.domain.AuthLoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthLoginAttemptRepository extends JpaRepository<AuthLoginAttempt, Long> {
}
