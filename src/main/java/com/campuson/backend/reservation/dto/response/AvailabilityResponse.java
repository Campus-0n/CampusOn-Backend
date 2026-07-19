package com.campuson.backend.reservation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record AvailabilityResponse(
        Long roomId,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        List<Slot> slots
) {
    public record Slot(
            @JsonFormat(pattern = "HH:mm") LocalTime startTime,
            @JsonFormat(pattern = "HH:mm") LocalTime endTime,
            boolean available
    ) {
    }
}
