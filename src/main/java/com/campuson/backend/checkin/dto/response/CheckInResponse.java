package com.campuson.backend.checkin.dto.response;

import com.campuson.backend.reservation.entity.Reservation;
import com.campuson.backend.reservation.entity.ReservationStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 체크인 성공 응답.
 *
 * @param reservationId  체크인된 예약 id
 * @param status         전환된 상태 (CHECKED_IN)
 * @param checkedInAt    체크인 시각
 * @param distanceMeters 강의실과의 실제 거리(m)
 */
public record CheckInResponse(
        Long reservationId,
        ReservationStatus status,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime checkedInAt,
        double distanceMeters
) {
    public static CheckInResponse of(Reservation r, double distanceMeters) {
        return new CheckInResponse(r.getId(), r.getStatus(), r.getCheckedInAt(), distanceMeters);
    }
}
