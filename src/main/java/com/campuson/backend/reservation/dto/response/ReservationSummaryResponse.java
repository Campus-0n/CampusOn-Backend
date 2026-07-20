package com.campuson.backend.reservation.dto.response;

import com.campuson.backend.reservation.entity.ReservationStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 내 예약 목록 아이템. 탭에 따라 채워지는 필드가 다르므로 null 필드는 응답에서 제외한다.
 * - UPCOMING: displayLabel 계산값 포함
 * - IN_USE: remainingMinutes, extensionCount 포함
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ReservationSummaryResponse(
        Long reservationId,
        String roomName,
        String building,
        String floor,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate reservationDate,
        @JsonFormat(pattern = "HH:mm") LocalTime startTime,
        @JsonFormat(pattern = "HH:mm") LocalTime endTime,
        ReservationStatus status,
        String displayLabel,
        Long remainingMinutes,
        Integer extensionCount
) {
}
