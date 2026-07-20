package com.campuson.backend.room.repository;

import com.campuson.backend.room.domain.RoomSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RoomScheduleRepository extends JpaRepository<RoomSchedule, Long> {

    // 겹침 조건: 기존블록.start < 조회구간.end AND 기존블록.end > 조회구간.start
    List<RoomSchedule> findByRoom_IdInAndStartTimeLessThanAndEndTimeGreaterThan(
            Collection<Long> roomIds, LocalDateTime windowEnd, LocalDateTime windowStart);

    List<RoomSchedule> findByRoom_IdAndStartTimeBetween(Long roomId, LocalDateTime rangeStart, LocalDateTime rangeEnd);

    Optional<RoomSchedule> findFirstByRoom_IdAndStartTimeAfterOrderByStartTimeAsc(Long roomId, LocalDateTime after);
}
