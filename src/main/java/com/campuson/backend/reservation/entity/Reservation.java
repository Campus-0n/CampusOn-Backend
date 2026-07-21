package com.campuson.backend.reservation.entity;

import com.campuson.backend.global.base.BaseEntity;
import com.campuson.backend.global.exception.BusinessException;
import com.campuson.backend.global.exception.ExceptionType;
import com.campuson.backend.reservation.ReservationPolicy;
import com.campuson.backend.room.domain.Room;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Reservation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private Long userId; // 예약자(호스트)

    @Column(nullable = false)
    private LocalDate reservationDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    private String purpose;

    private int headcount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    private int extensionCount;

    private LocalDateTime checkedInAt;

    @Builder
    public Reservation(Room room, Long userId, LocalDate reservationDate, LocalTime startTime,
                       LocalTime endTime, String purpose, int headcount) {
        this.room = room;
        this.userId = userId;
        this.reservationDate = reservationDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.purpose = purpose;
        this.headcount = headcount;
        this.status = ReservationStatus.RESERVED;
        this.extensionCount = 0;
    }

    // --- 시각 헬퍼 ---

    public LocalDateTime startDateTime() {
        return LocalDateTime.of(reservationDate, startTime);
    }

    public LocalDateTime endDateTime() {
        return LocalDateTime.of(reservationDate, endTime);
    }

    // --- 소유권 ---

    public boolean isOwnedBy(Long userId) {
        return this.userId.equals(userId);
    }

    public void validateOwner(Long userId) {
        if (!isOwnedBy(userId)) {
            throw new BusinessException(ExceptionType.NOT_RESERVATION_OWNER);
        }
    }

    // --- 상태 전이 ---

    /** 체크인(QR+GPS 검증은 서비스에서 수행) 후 이용중으로 전환. 기능③에서 사용 예정. */
    public void checkIn(LocalDateTime now) {
        if (this.status != ReservationStatus.RESERVED) {
            throw new BusinessException(ExceptionType.INVALID_STATUS);
        }
        this.status = ReservationStatus.CHECKED_IN;
        this.checkedInAt = now;
    }

    /** 사용자가 시작 전 직접 취소. */
    public void cancel() {
        if (this.status != ReservationStatus.RESERVED) {
            throw new BusinessException(ExceptionType.INVALID_STATUS);
        }
        this.status = ReservationStatus.CANCELLED;
    }

    /**
     * 이용 중(CHECKED_IN) 사용자가 강의실 허용 반경을 장시간 이탈 → 예약 자동취소.
     * (기능③ 위치추적에서 사용. 상태값은 사용자 직접취소와 동일하게 CANCELLED.)
     */
    public void cancelByLeaving() {
        if (this.status != ReservationStatus.CHECKED_IN) {
            throw new BusinessException(ExceptionType.INVALID_STATUS);
        }
        this.status = ReservationStatus.CANCELLED;
    }

    /** 이용중 예약을 사용자가 직접 종료. */
    public void end() {
        if (this.status != ReservationStatus.CHECKED_IN) {
            throw new BusinessException(ExceptionType.INVALID_STATUS);
        }
        this.status = ReservationStatus.COMPLETED;
    }

    /** 미체크인 유예 초과 → NO_SHOW (스케줄러). */
    public void markNoShow() {
        if (this.status == ReservationStatus.RESERVED) {
            this.status = ReservationStatus.NO_SHOW;
        }
    }

    /** 종료시각 도달 → COMPLETED (스케줄러). */
    public void markCompleted() {
        if (this.status == ReservationStatus.CHECKED_IN) {
            this.status = ReservationStatus.COMPLETED;
        }
    }

    /** 연장: 상태·횟수 검사 후 종료시각 갱신. 충돌검사는 서비스에서 선행. */
    public void extend(LocalTime newEndTime) {
        if (this.status != ReservationStatus.CHECKED_IN) {
            throw new BusinessException(ExceptionType.INVALID_STATUS);
        }
        if (this.extensionCount >= ReservationPolicy.MAX_EXTENSION_COUNT) {
            throw new BusinessException(ExceptionType.EXTENSION_LIMIT_EXCEEDED);
        }
        // 연장은 1시간 단위(현재 종료 + 1시간)만 허용
        if (!ReservationPolicy.isValidExtension(this.endTime, newEndTime)) {
            throw new BusinessException(ExceptionType.INVALID_EXTENSION_UNIT);
        }
        // 연장 후 종료시각도 운영시간(22:00)을 넘을 수 없음
        if (newEndTime.isAfter(ReservationPolicy.OPERATING_END)) {
            throw new BusinessException(ExceptionType.OUT_OF_OPERATING_HOURS);
        }
        this.endTime = newEndTime;
        this.extensionCount++;
    }
}
