package com.campuson.backend.room.service;

import com.campuson.backend.global.exception.BusinessException;
import com.campuson.backend.global.exception.ExceptionType;
import com.campuson.backend.room.domain.Room;
import com.campuson.backend.room.domain.RoomImage;
import com.campuson.backend.room.dto.request.RoomSearchRequest;
import com.campuson.backend.room.dto.response.RoomSearchResponse;
import com.campuson.backend.room.repository.RoomImageRepository;
import com.campuson.backend.room.repository.RoomRepository;
import com.campuson.backend.room.repository.RoomSpecification;
import com.campuson.backend.room.util.TimeSlot;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomSearchService {

    private final RoomRepository roomRepository;
    private final RoomImageRepository roomImageRepository;
    private final RoomAvailabilityService roomAvailabilityService;

    @Transactional(readOnly = true)
    public List<RoomSearchResponse> search(RoomSearchRequest request) {
        Specification<Room> spec = Specification.where(RoomSpecification.buildingIdEquals(request.buildingId()))
                .and(RoomSpecification.buildingNameContains(request.buildingName()))
                .and(RoomSpecification.roomNumberContains(request.roomNumber()));

        List<Room> rooms = roomRepository.findAll(spec);

        if (request.facilities() != null && !request.facilities().isEmpty()) {
            rooms = rooms.stream()
                    .filter(room -> room.getFacilities().containsAll(request.facilities()))
                    .toList();
        }

        LocalDateTime windowStart = resolveWindowStart(request);
        LocalDateTime windowEnd = resolveWindowEnd(request, windowStart);

        List<Long> roomIds = rooms.stream().map(Room::getId).toList();
        Map<Long, Boolean> availabilityMap = roomAvailabilityService.checkAvailability(roomIds, windowStart, windowEnd);

        List<RoomSearchResponse> responses = rooms.stream()
                .map(room -> toResponse(room, availabilityMap.getOrDefault(room.getId(), true), windowStart))
                .collect(Collectors.toCollection(ArrayList::new));

        if (request.availableOnly()) {
            responses = responses.stream()
                    .filter(RoomSearchResponse::available)
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        return responses;
    }

    private LocalDateTime resolveWindowStart(RoomSearchRequest request) {
        if (request.date() != null && request.startTime() != null) {
            return LocalDateTime.of(request.date(), request.startTime());
        }
        return LocalDateTime.now();
    }

    private LocalDateTime resolveWindowEnd(RoomSearchRequest request, LocalDateTime windowStart) {
        if (request.date() != null && request.endTime() != null) {
            return LocalDateTime.of(request.date(), request.endTime());
        }
        TimeSlot slot = TimeSlot.findCurrentOrNextSlot(windowStart.toLocalTime())
                .orElseThrow(() -> new BusinessException(ExceptionType.OUT_OF_OPERATING_HOURS));
        return LocalDateTime.of(windowStart.toLocalDate(), slot.getEnd());
    }

    private RoomSearchResponse toResponse(Room room, boolean available, LocalDateTime windowStart) {
        LocalDateTime availableUntil = available
                ? roomAvailabilityService.findAvailableUntil(windowStart)
                : null;

        String thumbnailUrl = roomImageRepository.findFirstByRoom_IdOrderBySortOrderAsc(room.getId())
                .map(RoomImage::getImageUrl)
                .orElse(null);

        return new RoomSearchResponse(
                room.getId(),
                room.getBuilding().getName(),
                room.getFloor(),
                room.getRoomNumber(),
                room.getCapacity(),
                available,
                availableUntil,
                new HashSet<>(room.getFacilities()), // ← 수정
                thumbnailUrl
        );
    }
}
