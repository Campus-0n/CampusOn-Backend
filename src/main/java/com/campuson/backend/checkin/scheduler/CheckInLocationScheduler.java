package com.campuson.backend.checkin.scheduler;

import com.campuson.backend.checkin.entity.CheckInSession;
import com.campuson.backend.checkin.repository.CheckInSessionRepository;
import com.campuson.backend.reservation.ReservationPolicy;
import com.campuson.backend.reservation.entity.Reservation;
import com.campuson.backend.reservation.entity.ReservationStatus;
import com.campuson.backend.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 이탈 자동취소 백스톱 스케줄러 (기능③).
 *
 * <p>웹앱이 위치 하트비트를 멈춘 채(예: 앱 종료·화면 이탈) 반경 밖에 머무는 경우를 대비해,
 * 반경 이탈({@code outOfRangeSince} != null)이 정책상 한계시간 이상 지속된 이용 중 예약을
 * 주기적으로(1분) 자동취소한다. 하트비트가 계속 오는 경우엔 서비스에서 먼저 취소된다.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CheckInLocationScheduler {

    private final CheckInSessionRepository checkInSessionRepository;
    private final ReservationRepository reservationRepository;

    @Scheduled(fixedRate = 60_000) // 60초
    @Transactional
    public void sweepLeftReservations() {
        LocalDateTime now = LocalDateTime.now();
        List<CheckInSession> outOfRange = checkInSessionRepository.findByOutOfRangeSinceIsNotNull();
        int count = 0;
        for (CheckInSession session : outOfRange) {
            if (!session.isOutOfRangeLongerThan(now, ReservationPolicy.CHECKIN_OUT_OF_RANGE_LIMIT_MINUTES)) {
                continue;
            }
            Reservation reservation = reservationRepository.findById(session.getReservationId()).orElse(null);
            if (reservation != null && reservation.getStatus() == ReservationStatus.CHECKED_IN) {
                reservation.cancelByLeaving();
                count++;
            }
        }
        if (count > 0) {
            log.info("[체크인 스케줄러] 반경 이탈 {}분 초과 자동취소 {}건",
                    ReservationPolicy.CHECKIN_OUT_OF_RANGE_LIMIT_MINUTES, count);
        }
    }
}
