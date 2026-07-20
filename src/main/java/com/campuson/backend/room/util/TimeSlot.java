package com.campuson.backend.room.util;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TimeSlot {

    public static final int SLOT_DURATION_MINUTES = 50;
    public static final int OPERATING_START_HOUR = 9;  // 09:00부터
    public static final int OPERATING_END_HOUR = 24;   // 23:00~23:50이 마지막 타임 (필요시 조정)

    private final LocalTime start;
    private final LocalTime end;

    private TimeSlot(LocalTime start, LocalTime end) {
        this.start = start;
        this.end = end;
    }

    public LocalTime getStart() { return start; }
    public LocalTime getEnd() { return end; }

    public static List<TimeSlot> allSlotsOfDay() {
        List<TimeSlot> slots = new ArrayList<>();
        for (int hour = OPERATING_START_HOUR; hour < OPERATING_END_HOUR; hour++) {
            LocalTime start = LocalTime.of(hour, 0);
            slots.add(new TimeSlot(start, start.plusMinutes(SLOT_DURATION_MINUTES)));
        }
        return slots;
    }

    /** 주어진 시각이 속한 타임, 쉬는시간이면 바로 다음 타임을 반환 (18:55에 조회하면 19:00~19:50 타임) */
    public static Optional<TimeSlot> findCurrentOrNextSlot(LocalTime time) {
        return allSlotsOfDay().stream()
                .filter(slot -> time.isBefore(slot.getEnd()))
                .findFirst();
    }
}
