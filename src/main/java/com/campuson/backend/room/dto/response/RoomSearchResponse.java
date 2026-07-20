package com.campuson.backend.room.dto.response;

import com.campuson.backend.room.domain.Facility;

import java.time.LocalDateTime;
import java.util.Set;

public record RoomSearchResponse(
        Long roomId,
        String buildingName,
        int floor,
        String roomNumber,
        int capacity,
        boolean available,
        LocalDateTime availableUntil, // available=false면 null
        Set<Facility> facilities,
        String thumbnailUrl
) {}
