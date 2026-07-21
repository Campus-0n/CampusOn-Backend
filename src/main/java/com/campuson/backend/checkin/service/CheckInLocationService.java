package com.campuson.backend.checkin.service;

import com.campuson.backend.checkin.dto.request.LocationReportRequest;
import com.campuson.backend.checkin.dto.response.LocationReportResponse;
import com.campuson.backend.checkin.entity.CheckInSession;
import com.campuson.backend.checkin.repository.CheckInSessionRepository;
import com.campuson.backend.global.exception.BusinessException;
import com.campuson.backend.global.exception.ExceptionType;
import com.campuson.backend.reservation.ReservationPolicy;
import com.campuson.backend.reservation.entity.Reservation;
import com.campuson.backend.reservation.entity.ReservationStatus;
import com.campuson.backend.reservation.repository.ReservationRepository;
import com.campuson.backend.room.domain.Spot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 이용 중 위치추적 서비스 (기능③).
 *
 * <p>웹앱이 보내는 위치 하트비트를 받아 강의실이 속한 거점({@link Spot})과의 거리를 계산하고,
 * 허용 반경 이탈이 {@link ReservationPolicy#CHECKIN_OUT_OF_RANGE_LIMIT_MINUTES}분 이상
 * 지속되면 예약을 자동취소한다. (앱이 하트비트를 멈춘 경우의 백스톱은 스케줄러가 담당.)</p>
 */
@Service
@RequiredArgsConstructor
public class CheckInLocationService {

    private final ReservationRepository reservationRepository;
    private final CheckInSessionRepository checkInSessionRepository;

    @Transactional
    public LocationReportResponse report(Long userId, Long reservationId, LocationReportRequest request) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ExceptionType.RESERVATION_NOT_FOUND));

        // 본인의, 이용 중(CHECKED_IN) 예약만 위치추적 대상
        reservation.validateOwner(userId);
        if (reservation.getStatus() != ReservationStatus.CHECKED_IN) {
            throw new BusinessException(ExceptionType.INVALID_STATUS);
        }

        Spot spot = CheckInService.requireSpot(reservation.getRoom());
        double distance = CheckInService.distanceToSpot(spot, request.latitude(), request.longitude());
        double allowedRadius = spot.getAllowedRadiusMeters() != null
                ? spot.getAllowedRadiusMeters()
                : ReservationPolicy.DEFAULT_CHECKIN_RADIUS_METERS;
        boolean inRange = distance <= allowedRadius;

        LocalDateTime now = LocalDateTime.now();
        CheckInSession session = checkInSessionRepository.findById(reservationId)
                .orElseGet(() -> new CheckInSession(reservationId));
        session.record(request.latitude(), request.longitude(), now, inRange);

        boolean autoCancelled = false;
        if (session.isOutOfRangeLongerThan(now, ReservationPolicy.CHECKIN_OUT_OF_RANGE_LIMIT_MINUTES)) {
            reservation.cancelByLeaving();
            autoCancelled = true;
        }
        checkInSessionRepository.save(session);

        return new LocationReportResponse(
                reservationId,
                reservation.getStatus(),
                inRange,
                distance,
                session.secondsUntilAutoCancel(now, ReservationPolicy.CHECKIN_OUT_OF_RANGE_LIMIT_MINUTES),
                autoCancelled);
    }
}
