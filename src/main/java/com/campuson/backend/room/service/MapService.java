package com.campuson.backend.room.service;


import com.campuson.backend.global.exception.BusinessException;
import com.campuson.backend.global.exception.ExceptionType;
import com.campuson.backend.room.domain.Building;
import com.campuson.backend.room.domain.Room;
import com.campuson.backend.room.dto.response.BuildingMapResponse;
import com.campuson.backend.room.dto.response.NearestRoomResponse;
import com.campuson.backend.room.repository.BuildingRepository;
import com.campuson.backend.room.repository.RoomRepository;
import com.campuson.backend.room.util.DistanceCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MapService {

    private final BuildingRepository buildingRepository;
    private final RoomRepository roomRepository;
    private final RoomAvailabilityService roomAvailabilityService;

    @Transactional(readOnly = true)
    public List<BuildingMapResponse> getBuildingMapInfo() {
        List<Building> buildings = buildingRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        return buildings.stream()
                .map(building -> new BuildingMapResponse(
                        building.getId(),
                        building.getName(),
                        building.getLatitude(),
                        building.getLongitude(),
                        countAvailable(building.getId(), now)
                ))
                .toList();
    }

    /** 가까운 건물부터 확인해서, 그 건물에서 가장 낮은 층의 이용 가능 강의실을 추천 */
    @Transactional(readOnly = true)
    public NearestRoomResponse recommendNearestAvailableRoom(double userLat, double userLng) {
        LocalDateTime now = LocalDateTime.now();

        List<Building> sortedByDistance = buildingRepository.findAll().stream()
                .sorted(Comparator.comparingDouble(b ->
                        DistanceCalculator.distanceInMeters(userLat, userLng, b.getLatitude(), b.getLongitude())))
                .toList();

        for (Building building : sortedByDistance) {
            List<Room> rooms = roomRepository.findByBuilding_Id(building.getId());
            List<Long> roomIds = rooms.stream().map(Room::getId).toList();
            Map<Long, Boolean> availabilityMap = roomAvailabilityService.checkAvailability(roomIds, now, now.plusMinutes(1));

            Optional<Room> lowestFloorAvailable = rooms.stream()
                    .filter(room -> Boolean.TRUE.equals(availabilityMap.get(room.getId())))
                    .min(Comparator.comparingInt(Room::getFloor));

            if (lowestFloorAvailable.isPresent()) {
                Room room = lowestFloorAvailable.get();
                double distance = DistanceCalculator.distanceInMeters(
                        userLat, userLng, building.getLatitude(), building.getLongitude());

                return new NearestRoomResponse(
                        building.getName(),
                        room.getFloor(),
                        room.getRoomNumber(),
                        room.getId(),
                        Math.round(distance)
                );
            }
        }

        throw new BusinessException(ExceptionType.NO_AVAILABLE_ROOM);
    }

    private int countAvailable(Long buildingId, LocalDateTime now) {
        List<Room> rooms = roomRepository.findByBuilding_Id(buildingId);
        List<Long> roomIds = rooms.stream().map(Room::getId).toList();
        Map<Long, Boolean> map = roomAvailabilityService.checkAvailability(roomIds, now, now.plusMinutes(1));
        return (int) map.values().stream().filter(Boolean::booleanValue).count();
    }
}
