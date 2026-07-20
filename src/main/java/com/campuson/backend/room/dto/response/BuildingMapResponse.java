package com.campuson.backend.room.dto.response;

public record BuildingMapResponse(
        Long buildingId,
        String buildingName,
        Double latitude,
        Double longitude,
        int availableRoomCount
) {}
