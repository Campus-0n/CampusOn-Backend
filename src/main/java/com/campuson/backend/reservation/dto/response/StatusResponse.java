package com.campuson.backend.reservation.dto.response;

import com.campuson.backend.reservation.entity.Reservation;
import com.campuson.backend.reservation.entity.ReservationStatus;

public record StatusResponse(
        Long reservationId,
        ReservationStatus status
) {
    public static StatusResponse of(Reservation r) {
        return new StatusResponse(r.getId(), r.getStatus());
    }
}
