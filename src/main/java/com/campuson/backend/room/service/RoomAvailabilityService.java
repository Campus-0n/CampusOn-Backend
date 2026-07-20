package com.campuson.backend.room.service;

import com.campuson.backend.global.exception.BusinessException;
import com.campuson.backend.global.exception.ExceptionType;
import com.campuson.backend.room.domain.RoomSchedule;
import com.campuson.backend.room.dto.response.RoomDetailResponse;
import com.campuson.backend.room.repository.RoomScheduleRepository;
import com.campuson.backend.room.util.TimeSlot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomAvailabilityService {

    private final RoomScheduleRepository roomScheduleRepository;

    /** 주어진 구간[windowStart, windowEnd)에 각 강의실이 이용 가능한지 한 번에 계산 (N+1 방지) */
    public Map<Long, Boolean> checkAvailability(List<Long> roomIds, LocalDateTime windowStart, LocalDateTime windowEnd) {
        if (roomIds.isEmpty()) return Map.of();

        List<RoomSchedule> overlapping = roomScheduleRepository
                .findByRoom_IdInAndStartTimeLessThanAndEndTimeGreaterThan(roomIds, windowEnd, windowStart);

        Set<Long> busyRoomIds = overlapping.stream()
                .map(s -> s.getRoom().getId())
                .collect(Collectors.toSet());

        return roomIds.stream()
                .collect(Collectors.toMap(id -> id, id -> !busyRoomIds.contains(id)));
    }

    /** "지금부터 언제까지 이용 가능한지" = 지금이 속한 50분 타임의 종료 시각 */
    public LocalDateTime findAvailableUntil(LocalDateTime from) {
        TimeSlot slot = TimeSlot.findCurrentOrNextSlot(from.toLocalTime())
                .orElseThrow(() -> new BusinessException(ExceptionType.OUT_OF_OPERATING_HOURS));
        return LocalDateTime.of(from.toLocalDate(), slot.getEnd());
    }

    /** 하루 전체를 50분 단위 타임으로 쪼개서 이용가능/불가 표시 (이제 빈 시간을 이어붙이지 않음) */
    public List<RoomDetailResponse.ScheduleBlock> buildDaySchedule(Long roomId, LocalDate date) {
        List<RoomSchedule> daySchedules = roomScheduleRepository.findByRoom_IdAndStartTimeBetween(
                roomId, date.atStartOfDay(), date.plusDays(1).atStartOfDay());

        return TimeSlot.allSlotsOfDay().stream()
                .map(slot -> {
                    LocalDateTime slotStart = LocalDateTime.of(date, slot.getStart());
                    LocalDateTime slotEnd = LocalDateTime.of(date, slot.getEnd());

                    Optional<RoomSchedule> overlapping = daySchedules.stream()
                            .filter(s -> s.getStartTime().isBefore(slotEnd) && s.getEndTime().isAfter(slotStart))
                            .findFirst();

                    return new RoomDetailResponse.ScheduleBlock(
                            slotStart,
                            slotEnd,
                            overlapping.isEmpty(),
                            overlapping.map(RoomSchedule::getTitle).orElse(null)
                    );
                })
                .toList();
    }
}
