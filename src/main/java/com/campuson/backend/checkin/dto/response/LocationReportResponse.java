package com.campuson.backend.checkin.dto.response;

import com.campuson.backend.reservation.entity.ReservationStatus;

/**
 * 위치 하트비트 응답.
 *
 * @param reservationId          예약 id
 * @param status                 현재 예약 상태 (자동취소되면 CANCELLED)
 * @param inRange                현재 허용 반경 안인지
 * @param distanceMeters         강의실과의 거리(m)
 * @param secondsUntilAutoCancel 반경 밖일 때 자동취소까지 남은 초 (반경 안이면 null)
 * @param autoCancelled          이번 보고로 자동취소가 발생했는지
 */
public record LocationReportResponse(
        Long reservationId,
        ReservationStatus status,
        boolean inRange,
        double distanceMeters,
        Long secondsUntilAutoCancel,
        boolean autoCancelled
) {
}
