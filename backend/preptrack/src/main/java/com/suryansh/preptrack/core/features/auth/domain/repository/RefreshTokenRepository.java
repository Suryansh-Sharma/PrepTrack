package com.suryansh.preptrack.core.features.auth.domain.repository;

import com.suryansh.preptrack.core.features.auth.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    @Query("select rt from RefreshToken rt where rt.user.id = :userId")
    List<RefreshToken> findByUserId(@Param("userId") Integer userId);

    @Modifying
    @Query("update RefreshToken rt set rt.revokedAt = current timestamp where rt.user.id = :userId")
    void LogoutAll(@Param("userId") Integer userId);

    @Modifying
    @Query("""
                update RefreshToken rt
                set rt.revokedAt = CURRENT_TIMESTAMP
                where rt.user.id = :userId
            """)
    void revokeAllByUserId(@Param("userId") Integer id);
}
