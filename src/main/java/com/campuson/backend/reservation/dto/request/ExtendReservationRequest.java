package com.campuson.backend.reservation.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record ExtendReservationRequest(
        @NotNull @JsonFormat(pattern = "HH:mm") LocalTime newEndTime
) {
}
