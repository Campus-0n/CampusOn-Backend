package com.campuson.backend.token.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;

@NoArgsConstructor
@Getter
@Entity
public class ConfirmationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String code;
    private LocalDateTime expiredAt;

    @Builder
    public ConfirmationToken(Long userId, String code, Long times) {
        this.userId = userId;
        this.code = code;
        this.expiredAt = LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusSeconds(times);
    }

    public boolean isExpired() {
        return LocalDateTime.now(ZoneId.of("Asia/Seoul")).isAfter(this.expiredAt);
    }
}
