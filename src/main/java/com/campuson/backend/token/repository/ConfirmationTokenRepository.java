package com.campuson.backend.token.repository;

import com.campuson.backend.token.entity.ConfirmationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfirmationTokenRepository extends JpaRepository<ConfirmationToken, Long> {
    Optional<ConfirmationToken> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}
