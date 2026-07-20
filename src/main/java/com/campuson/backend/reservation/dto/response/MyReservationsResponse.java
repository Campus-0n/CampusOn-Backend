package com.campuson.backend.reservation.dto.response;

import java.util.List;

public record MyReservationsResponse(
        String tab,
        List<ReservationSummaryResponse> reservations
) {
}
