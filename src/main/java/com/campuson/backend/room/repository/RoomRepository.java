package com.campuson.backend.room.repository;

import com.campuson.backend.room.entity.Room;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    /**
     * 예약 생성 시 동시성(레이스 컨디션) 방지를 위해 강의실 행에 비관적 쓰기 락을 건다.
     * 같은 강의실에 대한 동시 예약 요청을 직렬화하여 시간 충돌 검사를 안전하게 만든다.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Room> findWithLockById(Long id);
}
