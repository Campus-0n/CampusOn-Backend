package com.campuson.backend.reservation.entity;

/**
 * 예약 상태 흐름.
 * RESERVED ──check-in(QR+GPS)──▶ CHECKED_IN ──end()/종료시각도달──▶ COMPLETED
 *    │
 *    └ 미체크인(유예 초과) ──▶ NO_SHOW
 * CANCELLED : 시작 전 사용자가 직접 취소
 */
public enum ReservationStatus {
    RESERVED,
    CHECKED_IN,
    COMPLETED,
    CANCELLED,
    NO_SHOW
}
