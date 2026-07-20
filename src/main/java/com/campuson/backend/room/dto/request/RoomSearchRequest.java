package com.campuson.backend.room.dto.request;

import com.campuson.backend.room.domain.Facility;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record RoomSearchRequest(
        Long buildingId,
        String buildingName,
        String roomNumber,
        LocalDate date,       // null이면 오늘
        LocalTime startTime,  // null이면 지금 이 순간만 확인
        LocalTime endTime,
        List<Facility> facilities,
        boolean availableOnly // true면 위 조건대로 "지금/그 시간에 이용 가능한" 강의실만
) {}
