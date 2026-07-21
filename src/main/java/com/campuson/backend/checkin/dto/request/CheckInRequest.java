package com.campuson.backend.checkin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 강의실 체크인 요청 (QR + GPS 인증).
 *
 * @param roomId    스캔한 QR의 강의실 id. 예약의 room_id 와 일치해야 함
 * @param qrToken   강의실 QR에서 읽은 토큰
 * @param latitude  현재 위도
 * @param longitude 현재 경도
 */
public record CheckInRequest(
        @NotNull Long roomId,
        @NotBlank String qrToken,
        @NotNull Double latitude,
        @NotNull Double longitude
) {
}
