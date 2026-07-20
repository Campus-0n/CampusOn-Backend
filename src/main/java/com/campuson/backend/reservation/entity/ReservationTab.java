package com.campuson.backend.reservation.entity;

import java.util.List;

/**
 * 내 예약 목록의 상위 탭. (API 명세 2.2 탭↔상태 매핑)
 */
public enum ReservationTab {
    UPCOMING(List.of(ReservationStatus.RESERVED)),
    IN_USE(List.of(ReservationStatus.CHECKED_IN)),
    PAST(List.of(ReservationStatus.COMPLETED, ReservationStatus.CANCELLED, ReservationStatus.NO_SHOW));

    private final List<ReservationStatus> statuses;

    ReservationTab(List<ReservationStatus> statuses) {
        this.statuses = statuses;
    }

    public List<ReservationStatus> statuses() {
        return statuses;
    }
}
