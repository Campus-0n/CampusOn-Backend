package com.campuson.backend.reservation.controller;

import com.campuson.backend.global.response.ResponseBody;
import com.campuson.backend.reservation.dto.response.AvailabilityResponse;
import com.campuson.backend.reservation.service.AvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

import static com.campuson.backend.global.response.ResponseUtil.createSuccessResponse;

/**
 * 강의실 가용 시간대 조회. 기능①(강의실 검색)과 규칙 협의 대상.
 * 정식 Room/검색 API 병합 시 이 컨트롤러의 경로는 그대로 두고 서비스만 재사용 가능.
 */
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomAvailabilityController {

    private final AvailabilityService availabilityService;

    @GetMapping("/{roomId}/availability")
    public ResponseEntity<ResponseBody<AvailabilityResponse>> getAvailability(
            @PathVariable Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        AvailabilityResponse response = availabilityService.getAvailability(roomId, date);
        return ResponseEntity.ok(createSuccessResponse(response));
    }
}
