package com.campuson.backend.room.service;

import com.campuson.backend.global.exception.BusinessException;
import com.campuson.backend.global.exception.ExceptionType;
import com.campuson.backend.room.domain.Room;
import com.campuson.backend.room.domain.RoomImage;
import com.campuson.backend.room.dto.response.RoomDetailResponse;
import com.campuson.backend.room.repository.RoomImageRepository;
import com.campuson.backend.room.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomDetailService {

    private final RoomRepository roomRepository;
    private final RoomImageRepository roomImageRepository;
    private final RoomAvailabilityService roomAvailabilityService;

    @Transactional(readOnly = true)
    public RoomDetailResponse getDetail(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ExceptionType.ROOM_NOT_FOUND));

        List<String> imageUrls = roomImageRepository.findByRoom_IdOrderBySortOrderAsc(roomId)
                .stream()
                .map(RoomImage::getImageUrl)
                .toList();

        List<RoomDetailResponse.ScheduleBlock> todaySchedule =
                roomAvailabilityService.buildDaySchedule(roomId, LocalDate.now());

        return new RoomDetailResponse(
                room.getId(),
                room.getBuilding().getName(),
                room.getFloor(),
                room.getRoomNumber(),
                room.getCapacity(),
                new HashSet<>(room.getFacilities()), // ← 수정
                room.getUsageRule(),
                imageUrls,
                todaySchedule
        );
    }
}
