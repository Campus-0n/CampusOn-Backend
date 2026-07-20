package com.campuson.backend.reservation.service;

import com.campuson.backend.global.exception.BusinessException;
import com.campuson.backend.global.exception.ExceptionType;
import com.campuson.backend.reservation.ReservationPolicy;
import com.campuson.backend.reservation.dto.response.AvailabilityResponse;
import com.campuson.backend.reservation.entity.Reservation;
import com.campuson.backend.reservation.entity.ReservationStatus;
import com.campuson.backend.reservation.repository.ReservationRepository;
import com.campuson.backend.room.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private static final List<ReservationStatus> ACTIVE_STATUSES =
            List.of(ReservationStatus.RESERVED, ReservationStatus.CHECKED_IN);

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;

    /**
     * 특정 강의실·날짜의 1시간 단위 슬롯(n시00분~n시50분) 가용 여부.
     * 기존 예약 + 종료 버퍼(10분)를 반영한다. 기능①과 규칙 협의 후 조정 가능.
     */
    @Transactional(readOnly = true)
    public AvailabilityResponse getAvailability(Long roomId, LocalDate date) {
        if (!roomRepository.existsById(roomId)) {
            throw new BusinessException(ExceptionType.ROOM_NOT_FOUND);
        }

        List<Reservation> active =
                reservationRepository.findActiveByRoomAndDate(roomId, date, ACTIVE_STATUSES);

        List<AvailabilityResponse.Slot> slots = new ArrayList<>();
        LocalTime slotStart = ReservationPolicy.OPERATING_START;
        while (true) {
            LocalTime slotEnd = slotStart.withMinute(ReservationPolicy.REQUIRED_END_MINUTE);
            if (slotEnd.isAfter(ReservationPolicy.OPERATING_END)) {
                break;
            }
            LocalTime finalStart = slotStart;
            boolean available = active.stream().noneMatch(r ->
                    ReservationPolicy.conflicts(finalStart, slotEnd, r.getStartTime(), r.getEndTime()));
            slots.add(new AvailabilityResponse.Slot(slotStart, slotEnd, available));

            slotStart = slotStart.plusHours(1);
            if (slotStart.getHour() == 0) { // 자정 래핑 방지
                break;
            }
        }

        return new AvailabilityResponse(roomId, date, slots);
    }
}
