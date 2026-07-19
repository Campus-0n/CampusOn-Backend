package com.campuson.backend.reservation.controller;

import com.campuson.backend.global.jwt.JwtAuthentication;
import com.campuson.backend.global.response.ResponseBody;
import com.campuson.backend.reservation.dto.request.CreateReservationRequest;
import com.campuson.backend.reservation.dto.request.ExtendReservationRequest;
import com.campuson.backend.reservation.dto.response.CreateReservationResponse;
import com.campuson.backend.reservation.dto.response.ExtendReservationResponse;
import com.campuson.backend.reservation.dto.response.MyReservationsResponse;
import com.campuson.backend.reservation.dto.response.ReservationDetailResponse;
import com.campuson.backend.reservation.dto.response.StatusResponse;
import com.campuson.backend.reservation.entity.ReservationStatus;
import com.campuson.backend.reservation.entity.ReservationTab;
import com.campuson.backend.reservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.campuson.backend.global.response.ResponseUtil.createSuccessResponse;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ResponseBody<CreateReservationResponse>> create(
            Authentication authentication,
            @RequestBody @Valid CreateReservationRequest request) {
        CreateReservationResponse response = reservationService.create(userId(authentication), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createSuccessResponse(response));
    }

    @GetMapping("/me")
    public ResponseEntity<ResponseBody<MyReservationsResponse>> getMyReservations(
            Authentication authentication,
            @RequestParam(required = false) ReservationTab tab,
            @RequestParam(required = false) ReservationStatus status) {
        MyReservationsResponse response = reservationService.getMyReservations(userId(authentication), tab, status);
        return ResponseEntity.ok(createSuccessResponse(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseBody<ReservationDetailResponse>> getDetail(
            Authentication authentication,
            @PathVariable Long id) {
        ReservationDetailResponse response = reservationService.getDetail(userId(authentication), id);
        return ResponseEntity.ok(createSuccessResponse(response));
    }

    @PatchMapping("/{id}/extend")
    public ResponseEntity<ResponseBody<ExtendReservationResponse>> extend(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody @Valid ExtendReservationRequest request) {
        ExtendReservationResponse response = reservationService.extend(userId(authentication), id, request);
        return ResponseEntity.ok(createSuccessResponse(response));
    }

    @PatchMapping("/{id}/end")
    public ResponseEntity<ResponseBody<StatusResponse>> end(
            Authentication authentication,
            @PathVariable Long id) {
        StatusResponse response = reservationService.end(userId(authentication), id);
        return ResponseEntity.ok(createSuccessResponse(response));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ResponseBody<StatusResponse>> cancel(
            Authentication authentication,
            @PathVariable Long id) {
        StatusResponse response = reservationService.cancel(userId(authentication), id);
        return ResponseEntity.ok(createSuccessResponse(response));
    }

    private Long userId(Authentication authentication) {
        return ((JwtAuthentication) authentication).userId();
    }
}
