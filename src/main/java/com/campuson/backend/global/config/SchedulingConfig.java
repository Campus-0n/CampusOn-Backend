package com.campuson.backend.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 미체크인 자동취소(NO_SHOW) / 종료시각 도달(COMPLETED) 배치를 위한 스케줄링 활성화.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
