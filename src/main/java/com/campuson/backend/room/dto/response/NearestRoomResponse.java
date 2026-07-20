package com.campuson.backend.room.dto.response;

public record NearestRoomResponse(
        String buildingName,
        int floor,
        String roomNumber,
        Long roomId,
        long distanceInMeters
) {}
