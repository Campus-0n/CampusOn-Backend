package com.campuson.backend.checkin.repository;

import com.campuson.backend.checkin.entity.CheckInSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 위치추적 세션 저장소. PK가 reservationId 이므로 {@code findById(reservationId)} 로 조회한다.
 */
public interface CheckInSessionRepository extends JpaRepository<CheckInSession, Long> {

    /** 현재 반경 밖(이탈 중)인 세션만. 스케줄러 백스톱에서 사용. */
    List<CheckInSession> findByOutOfRangeSinceIsNotNull();
}
