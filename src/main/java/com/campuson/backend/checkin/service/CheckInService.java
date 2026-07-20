package com.campuson.backend.checkin.service;

import com.campuson.backend.checkin.dto.request.CheckInRequest;
import com.campuson.backend.checkin.dto.response.CheckInResponse;
import com.campuson.backend.global.exception.BusinessException;
import com.campuson.backend.global.exception.ExceptionType;
import com.campuson.backend.global.util.GeoUtil;
import com.campuson.backend.reservation.ReservationPolicy;
import com.campuson.backend.reservation.entity.Reservation;
import com.campuson.backend.reservation.repository.ReservationRepository;
import com.campuson.backend.room.domain.Room;
import com.campuson.backend.room.domain.Spot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 강의실 체크인(QR + GPS 인증) 서비스.
 *
 * <p>위치·QR 정보는 room 도메인 계층(건물 → 거점 → 강의실)에서 읽는다.
 * GPS 좌표·허용반경은 강의실이 속한 거점({@link Spot})에서, QR 토큰은 강의실({@link Room})에서 가져온다.</p>
 */
@Service
@RequiredArgsConstructor
public class CheckInService {

    private final ReservationRepository reservationRepository;

    /**
     * 강의실 체크인. 아래 4가지 조건을 모두 통과하면 예약을 CHECKED_IN 으로 전환한다.
     * <ol>
     *   <li>본인의 예약인지 (NOT_RESERVATION_OWNER)</li>
     *   <li>체크인 가능 시간창 안인지 (CHECKIN_TIME_WINDOW)</li>
     *   <li>스캔한 QR(roomId + qrToken)이 예약한 강의실과 일치하는지 (CHECKIN_INVALID_QR)</li>
     *   <li>현재 위치가 거점 허용 반경 안인지 (CHECKIN_OUT_OF_RANGE)</li>
     * </ol>
     */
    @Transactional
    public CheckInResponse checkIn(Long userId, Long reservationId, CheckInRequest request) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ExceptionType.RESERVATION_NOT_FOUND));

        // 1) 소유권
        reservation.validateOwner(userId);

        // 2) 체크인 시간창
        if (!ReservationPolicy.isWithinCheckInWindow(reservation.startDateTime(), LocalDateTime.now())) {
            throw new BusinessException(ExceptionType.CHECKIN_TIME_WINDOW);
        }

        Room room = reservation.getRoom();

        // 3) QR 검증: 스캔한 강의실 id·토큰이 예약한 강의실과 일치해야 함
        if (!room.getId().equals(request.roomId())
                || room.getQrToken() == null
                || !room.getQrToken().equals(request.qrToken())) {
            throw new BusinessException(ExceptionType.CHECKIN_INVALID_QR);
        }

        // 4) GPS 검증: 거점 좌표와의 거리가 허용 반경 이내여야 함
        Spot spot = requireSpot(room);
        double allowedRadius = spot.getAllowedRadiusMeters() != null
                ? spot.getAllowedRadiusMeters()
                : ReservationPolicy.DEFAULT_CHECKIN_RADIUS_METERS;
        double distance = distanceToSpot(spot, request.latitude(), request.longitude());

        if (distance > allowedRadius) {
            throw new BusinessException(ExceptionType.CHECKIN_OUT_OF_RANGE,
                    String.format("강의실 반경(%.0fm) 밖입니다. 현재 거리 %.1fm.", allowedRadius, distance));
        }

        // 모든 조건 통과 → 상태 전이(RESERVED → CHECKED_IN). 상태 검증은 엔티티에서 수행.
        reservation.checkIn(LocalDateTime.now());
        return CheckInResponse.of(reservation, distance);
    }

    /** 강의실의 거점(좌표) 확보. 거점·좌표 미설정 시 예외. */
    static Spot requireSpot(Room room) {
        Spot spot = room.getSpot();
        if (spot == null || spot.getLatitude() == null || spot.getLongitude() == null) {
            throw new BusinessException(ExceptionType.CHECKIN_OUT_OF_RANGE,
                    "강의실 위치 정보가 설정되지 않았습니다.");
        }
        return spot;
    }

    /** 거점 좌표와 현재 위치 사이 거리(m, 소수 첫째자리 반올림). */
    static double distanceToSpot(Spot spot, double latitude, double longitude) {
        double distance = GeoUtil.distanceMeters(
                spot.getLatitude(), spot.getLongitude(), latitude, longitude);
        return Math.round(distance * 10) / 10.0;
    }
}
