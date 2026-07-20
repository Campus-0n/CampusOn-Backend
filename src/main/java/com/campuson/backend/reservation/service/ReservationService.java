package com.campuson.backend.reservation.service;

import com.campuson.backend.global.exception.BusinessException;
import com.campuson.backend.global.exception.ExceptionType;
import com.campuson.backend.reservation.ReservationPolicy;
import com.campuson.backend.reservation.dto.request.CreateReservationRequest;
import com.campuson.backend.reservation.dto.request.ExtendReservationRequest;
import com.campuson.backend.reservation.dto.response.CreateReservationResponse;
import com.campuson.backend.reservation.dto.response.ExtendReservationResponse;
import com.campuson.backend.reservation.dto.response.MyReservationsResponse;
import com.campuson.backend.reservation.dto.response.ReservationDetailResponse;
import com.campuson.backend.reservation.dto.response.ReservationSummaryResponse;
import com.campuson.backend.reservation.dto.response.StatusResponse;
import com.campuson.backend.reservation.entity.Reservation;
import com.campuson.backend.reservation.entity.ReservationStatus;
import com.campuson.backend.reservation.entity.ReservationTab;
import com.campuson.backend.reservation.repository.ReservationRepository;
import com.campuson.backend.room.domain.Room;
import com.campuson.backend.room.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private static final List<ReservationStatus> ACTIVE_STATUSES =
            List.of(ReservationStatus.RESERVED, ReservationStatus.CHECKED_IN);

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;

    // ===== 생성 =====

    @Transactional
    public CreateReservationResponse create(Long userId, CreateReservationRequest request) {
        if (!ReservationPolicy.isValidEndMinute(request.endTime())) {
            throw new BusinessException(ExceptionType.INVALID_END_TIME);
        }
        if (!request.startTime().isBefore(request.endTime())) {
            throw new BusinessException(ExceptionType.INVALID_TIME_RANGE);
        }

        // 동시 예약 레이스 방지: 강의실 행에 비관적 락
        Room room = roomRepository.findWithLockById(request.roomId())
                .orElseThrow(() -> new BusinessException(ExceptionType.ROOM_NOT_FOUND));

        if (request.headcount() <= 0 || request.headcount() > room.getCapacity()) {
            throw new BusinessException(ExceptionType.INVALID_HEADCOUNT);
        }

        validateReservationTime(request.startTime(), request.endTime());

        validateNoConflict(room.getId(), request.reservationDate(),
                request.startTime(), request.endTime(), null);

        Reservation reservation = Reservation.builder()
                .room(room)
                .userId(userId)
                .reservationDate(request.reservationDate())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .purpose(request.purpose())
                .headcount(request.headcount())
                .build();

        reservationRepository.save(reservation);
        return CreateReservationResponse.of(reservation);
    }

    // ===== 목록 =====

    @Transactional(readOnly = true)
    public MyReservationsResponse getMyReservations(Long userId, ReservationTab tab, ReservationStatus status) {
        List<ReservationStatus> statuses = resolveStatuses(tab, status);

        List<Reservation> reservations = (statuses == null)
                ? reservationRepository.findByUserIdOrderByReservationDateDescStartTimeDesc(userId)
                : reservationRepository.findByUserIdAndStatusInOrderByReservationDateDescStartTimeDesc(userId, statuses);

        LocalDateTime now = LocalDateTime.now();
        List<ReservationSummaryResponse> items = reservations.stream()
                .map(r -> toSummary(r, now))
                .toList();

        return new MyReservationsResponse(tab != null ? tab.name() : null, items);
    }

    /** tab·status 조합을 조회할 상태 목록으로 변환. 둘 다 없으면 null(전체). */
    private List<ReservationStatus> resolveStatuses(ReservationTab tab, ReservationStatus status) {
        if (tab != null && status != null) {
            if (!tab.statuses().contains(status)) {
                return List.of(); // 탭 범위 밖 상태 → 교집합 없음
            }
            return List.of(status);
        }
        if (tab != null) {
            return tab.statuses();
        }
        if (status != null) {
            return List.of(status);
        }
        return null;
    }

    private ReservationSummaryResponse toSummary(Reservation r, LocalDateTime now) {
        String displayLabel = null;
        Long remainingMinutes = null;
        Integer extensionCount = null;

        if (r.getStatus() == ReservationStatus.RESERVED) {
            displayLabel = computeDisplayLabel(r, now);
        } else if (r.getStatus() == ReservationStatus.CHECKED_IN) {
            remainingMinutes = remainingMinutes(r, now);
            extensionCount = r.getExtensionCount();
        }

        Room room = r.getRoom();
        return new ReservationSummaryResponse(
                r.getId(),
                room.displayName(),
                room.getBuilding().getName(),
                room.getFloor()+"층",
                r.getReservationDate(),
                r.getStartTime(),
                r.getEndTime(),
                r.getStatus(),
                displayLabel,
                remainingMinutes,
                extensionCount
        );
    }

    /** 예정 탭 표시 라벨: 체크인 사전 허용 시간창 진입 여부로 결정. */
    private String computeDisplayLabel(Reservation r, LocalDateTime now) {
        LocalDateTime windowStart = r.startDateTime().minusMinutes(ReservationPolicy.CHECKIN_PRE_ALLOW_MINUTES);
        return !now.isBefore(windowStart) ? "체크인 대기 중" : "예약됨";
    }

    private Long remainingMinutes(Reservation r, LocalDateTime now) {
        long minutes = Duration.between(now, r.endDateTime()).toMinutes();
        return Math.max(0, minutes);
    }

    // ===== 상세 =====

    @Transactional(readOnly = true)
    public ReservationDetailResponse getDetail(Long userId, Long reservationId) {
        Reservation r = getOwnedReservation(userId, reservationId);
        LocalDateTime now = LocalDateTime.now();

        Long remainingMinutes = (r.getStatus() == ReservationStatus.CHECKED_IN)
                ? remainingMinutes(r, now) : null;

        LocalTime nextStart = findNextReservationStartTime(r);

        Room room = r.getRoom();

        return new ReservationDetailResponse(
                r.getId(),
                new ReservationDetailResponse.RoomSummary(room.getId(), room.displayName(), room.getCapacity()),
                r.getReservationDate(),
                r.getStartTime(),
                r.getEndTime(),
                r.getStatus(),
                r.getPurpose(),
                r.getHeadcount(),
                r.getExtensionCount(),
                r.getCheckedInAt(),
                remainingMinutes,
                nextStart
        );
    }

    /** 같은 강의실·날짜에서 이 예약 종료 이후 가장 빠른 다음 활성 예약의 시작시각. */
    private LocalTime findNextReservationStartTime(Reservation r) {
        return reservationRepository
                .findActiveByRoomAndDate(r.getRoom().getId(), r.getReservationDate(), ACTIVE_STATUSES).stream()
                .filter(other -> !other.getId().equals(r.getId()))
                .map(Reservation::getStartTime)
                .filter(start -> !start.isBefore(r.getEndTime()))
                .min(LocalTime::compareTo)
                .orElse(null);
    }

    // ===== 연장 =====

    @Transactional
    public ExtendReservationResponse extend(Long userId, Long reservationId, ExtendReservationRequest request) {
        Reservation r = getOwnedReservation(userId, reservationId);
        LocalTime newEndTime = request.newEndTime();

        if (!ReservationPolicy.isValidEndMinute(newEndTime)) {
            throw new BusinessException(ExceptionType.INVALID_END_TIME);
        }
        // 연장 후 시간대 충돌검사(자기 자신 제외)
        validateNoConflict(r.getRoom().getId(), r.getReservationDate(),
                r.getStartTime(), newEndTime, r.getId());

        r.extend(newEndTime); // 상태·횟수 검사 포함
        return ExtendReservationResponse.of(r);
    }

    // ===== 종료 / 취소 =====

    @Transactional
    public StatusResponse end(Long userId, Long reservationId) {
        Reservation r = getOwnedReservation(userId, reservationId);
        r.end();
        return StatusResponse.of(r);
    }

    @Transactional
    public StatusResponse cancel(Long userId, Long reservationId) {
        Reservation r = getOwnedReservation(userId, reservationId);
        r.cancel();
        return StatusResponse.of(r);
    }

    // ===== 공통 헬퍼 =====

    private Reservation getOwnedReservation(Long userId, Long reservationId) {
        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ExceptionType.RESERVATION_NOT_FOUND));
        r.validateOwner(userId);
        return r;
    }

    /** 같은 강의실+날짜의 활성 예약과 버퍼 포함 충돌 시 예외. excludeId 는 검사에서 제외. */
    private void validateNoConflict(Long roomId, LocalDate date,
                                    LocalTime start, LocalTime end, Long excludeId) {
        boolean conflict = reservationRepository.findActiveByRoomAndDate(roomId, date, ACTIVE_STATUSES).stream()
                .filter(existing -> excludeId == null || !existing.getId().equals(excludeId))
                .anyMatch(existing -> ReservationPolicy.conflicts(
                        start, end, existing.getStartTime(), existing.getEndTime()));
        if (conflict) {
            throw new BusinessException(ExceptionType.TIME_CONFLICT);
        }
    }

    /** 예약 시간 규칙: 운영시간(09:00~22:00) + 종료 :50 + 2시간 단위(정시 시작). */
    private void validateReservationTime(LocalTime start, LocalTime end) {
        if (!ReservationPolicy.isWithinOperatingHours(start, end)) {
            throw new BusinessException(ExceptionType.OUT_OF_OPERATING_HOURS);
        }
        if (!ReservationPolicy.isValidEndMinute(end)) {
            throw new BusinessException(ExceptionType.INVALID_END_TIME);
        }
        if (!ReservationPolicy.isValidReservationBlock(start, end)) {
            throw new BusinessException(ExceptionType.INVALID_RESERVATION_DURATION);
        }
    }
}
