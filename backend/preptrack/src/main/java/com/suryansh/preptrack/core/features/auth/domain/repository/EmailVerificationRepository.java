package com.suryansh.preptrack.core.features.auth.domain.repository;

import com.suryansh.preptrack.core.features.auth.domain.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerificationToken, String> {
    Optional<EmailVerificationToken> findTopByUserIdOrderByCreatedAtDesc(Integer userId);

    @Modifying
    @Query("""
                DELETE FROM EmailVerificationToken t
                WHERE t.user.id = :userId
                  AND t.expiresAt < :now
            """)
    void deleteExpiredTokens(@Param("userId") Integer userId, @Param("now") Instant now);
}
