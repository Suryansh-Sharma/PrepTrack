package com.suryansh.preptrack.core.features.auth.domain.repository;

import com.suryansh.preptrack.core.features.auth.domain.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, String> {
    Optional<PasswordResetToken> findTopByUserIdOrderByCreatedAtDesc(Integer userId);

    @Modifying
    @Query("delete from PasswordResetToken t where t.user.id = :userId and t.expiresAt < :now")
    void deleteExpiredTokenForUser(@Param("userId") Integer userId, @Param("now") Instant now);
}
