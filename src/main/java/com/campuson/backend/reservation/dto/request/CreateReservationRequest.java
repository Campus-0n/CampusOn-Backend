package com.campuson.backend.reservation.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.time.LocalTime;

public record CreateReservationRequest(
        @NotNull Long roomId,
        @NotNull @JsonFormat(pattern = "yyyy-MM-dd") LocalDate reservationDate,
        @NotNull @JsonFormat(pattern = "HH:mm") LocalTime startTime,
        @NotNull @JsonFormat(pattern = "HH:mm") LocalTime endTime,
        @NotBlank String purpose,
        @Positive int headcount
) {
}
