package com.campuson.backend.reservation.dto.response;

import com.campuson.backend.reservation.entity.ReservationStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ReservationDetailResponse(
        Long reservationId,
        RoomSummary room,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate reservationDate,
        @JsonFormat(pattern = "HH:mm") LocalTime startTime,
        @JsonFormat(pattern = "HH:mm") LocalTime endTime,
        ReservationStatus status,
        String purpose,
        int headcount,
        int extensionCount,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime checkedInAt,
        Long remainingMinutes,
        @JsonFormat(pattern = "HH:mm") LocalTime nextReservationStartTime
) {
    public record RoomSummary(Long roomId, String name, int capacity) {
    }
}
