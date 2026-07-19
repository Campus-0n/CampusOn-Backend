package com.campuson.backend.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * BaseEntity 의 createdAt / updatedAt 감사값을 채우기 위한 설정.
 * (기존에 누락되어 있어 예약 도메인에서 타임스탬프가 채워지도록 추가)
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
