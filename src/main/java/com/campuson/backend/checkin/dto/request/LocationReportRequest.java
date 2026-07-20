package com.campuson.backend.checkin.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * 이용 중 위치 하트비트 요청. 웹앱이 주기적으로(예: 30초~1분) 현재 위치를 전송한다.
 *
 * @param latitude  현재 위도
 * @param longitude 현재 경도
 */
public record LocationReportRequest(
        @NotNull Double latitude,
        @NotNull Double longitude
) {
}
