package com.suryansh.preptrack.core.features.auth.domain.repository;

import com.suryansh.preptrack.core.features.auth.domain.PasswordHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, Long> {
    List<PasswordHistory> findTop5ByUserIdOrderByCreatedAtDesc(
            Integer userId
    );
}