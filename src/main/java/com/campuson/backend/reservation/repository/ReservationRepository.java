package com.campuson.backend.reservation.repository;

import com.campuson.backend.reservation.entity.Reservation;
import com.campuson.backend.reservation.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /** 내 예약 전체 (최신순). */
    List<Reservation> findByUserIdOrderByReservationDateDescStartTimeDesc(Long userId);

    /** 내 예약 중 특정 상태들만 (최신순). */
    List<Reservation> findByUserIdAndStatusInOrderByReservationDateDescStartTimeDesc(
            Long userId, Collection<ReservationStatus> statuses);

    /**
     * 충돌검사·다음예약 계산용: 같은 강의실+날짜의 활성 예약(RESERVED/CHECKED_IN).
     */
    @Query("""
            select r from Reservation r
            where r.room.id = :roomId
              and r.reservationDate = :date
              and r.status in :statuses
            order by r.startTime asc
            """)
    List<Reservation> findActiveByRoomAndDate(@Param("roomId") Long roomId,
                                              @Param("date") LocalDate date,
                                              @Param("statuses") Collection<ReservationStatus> statuses);

    /** 스케줄러용: 특정 상태의 모든 예약. */
    List<Reservation> findByStatus(ReservationStatus status);
}
