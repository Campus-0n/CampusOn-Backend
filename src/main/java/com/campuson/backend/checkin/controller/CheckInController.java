package com.campuson.backend.checkin.controller;

import com.campuson.backend.checkin.dto.request.CheckInRequest;
import com.campuson.backend.checkin.dto.response.CheckInResponse;
import com.campuson.backend.checkin.service.CheckInService;
import com.campuson.backend.global.jwt.JwtAuthentication;
import com.campuson.backend.global.response.ResponseBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.campuson.backend.global.response.ResponseUtil.createSuccessResponse;

/**
 * 강의실 체크인(QR + GPS 인증) API.
 * 예약 리소스 하위 경로를 쓰지만 예약 컨트롤러와 분리된 별도 모듈이다.
 */
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInService checkInService;

    @PostMapping("/{id}/check-in")
    public ResponseEntity<ResponseBody<CheckInResponse>> checkIn(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody @Valid CheckInRequest request) {
        CheckInResponse response = checkInService.checkIn(userId(authentication), id, request);
        return ResponseEntity.ok(createSuccessResponse(response));
    }

    private Long userId(Authentication authentication) {
        return ((JwtAuthentication) authentication).userId();
    }
}
