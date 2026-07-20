package com.campuson.backend.room.dto.response;

import com.campuson.backend.room.domain.Facility;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public record RoomDetailResponse(
        Long roomId,
        String buildingName,
        int floor,
        String roomNumber,
        int capacity,
        Set<Facility> facilities,
        String usageRule,
        List<String> imageUrls,
        List<ScheduleBlock> todaySchedule
) {
    public record ScheduleBlock(
            LocalDateTime startTime,
            LocalDateTime endTime,
            boolean available,
            String title
    ) {}
}
