package com.campuson.backend.reservation.dto.response;

import com.campuson.backend.reservation.entity.Reservation;
import com.campuson.backend.reservation.entity.ReservationStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record CreateReservationResponse(
        Long reservationId,
        Long roomId,
        ReservationStatus status,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate reservationDate,
        @JsonFormat(pattern = "HH:mm") LocalTime startTime,
        @JsonFormat(pattern = "HH:mm") LocalTime endTime,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime createdAt
) {
    public static CreateReservationResponse of(Reservation r) {
        return new CreateReservationResponse(
                r.getId(),
                r.getRoom().getId(),
                r.getStatus(),
                r.getReservationDate(),
                r.getStartTime(),
                r.getEndTime(),
                r.getCreatedAt()
        );
    }
}
