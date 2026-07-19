package com.campuson.backend.reservation.scheduler;

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
 * 예약 상태 자동 전환 배치.
 * - RESERVED 이면서 (시작시각 + 유예)를 지나도 미체크인 → NO_SHOW
 * - CHECKED_IN 이면서 종료시각 도달 → COMPLETED
 * 별도 API 없이 주기적으로 처리한다(1분 주기).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationScheduler {

    private final ReservationRepository reservationRepository;

    @Scheduled(fixedRate = 60_000) // 60초
    @Transactional
    public void sweep() {
        LocalDateTime now = LocalDateTime.now();
        markNoShows(now);
        markCompleted(now);
    }

    private void markNoShows(LocalDateTime now) {
        List<Reservation> reserved = reservationRepository.findByStatus(ReservationStatus.RESERVED);
        int count = 0;
        for (Reservation r : reserved) {
            LocalDateTime deadline = r.startDateTime().plusMinutes(ReservationPolicy.CHECKIN_GRACE_MINUTES);
            if (now.isAfter(deadline)) {
                r.markNoShow();
                count++;
            }
        }
        if (count > 0) {
            log.info("[스케줄러] 미체크인 자동취소(NO_SHOW) {}건", count);
        }
    }

    private void markCompleted(LocalDateTime now) {
        List<Reservation> inUse = reservationRepository.findByStatus(ReservationStatus.CHECKED_IN);
        int count = 0;
        for (Reservation r : inUse) {
            if (!now.isBefore(r.endDateTime())) {
                r.markCompleted();
                count++;
            }
        }
        if (count > 0) {
            log.info("[스케줄러] 종료시각 도달 COMPLETED {}건", count);
        }
    }
}
