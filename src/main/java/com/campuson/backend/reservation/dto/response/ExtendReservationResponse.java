package com.campuson.backend.reservation.dto.response;

import com.campuson.backend.reservation.entity.Reservation;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalTime;

public record ExtendReservationResponse(
        Long reservationId,
        @JsonFormat(pattern = "HH:mm") LocalTime endTime,
        int extensionCount
) {
    public static ExtendReservationResponse of(Reservation r) {
        return new ExtendReservationResponse(r.getId(), r.getEndTime(), r.getExtensionCount());
    }
}
