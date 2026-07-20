package com.campuson.backend.room.init;

import com.campuson.backend.room.domain.*;
import com.campuson.backend.room.repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "app.seed-data.enabled", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
public class RoomDataInitializer implements ApplicationRunner {

    private final BuildingRepository buildingRepository;
    private final RoomRepository roomRepository;
    private final RoomImageRepository roomImageRepository;

    private static final Random RANDOM = new Random();

    private record BuildingSeed(String name, String address, double lat, double lng) {}
    private record RoomSeed(String buildingName, int floor, String roomNumber, int capacity, Set<Facility> facilities) {}

    private static final List<BuildingSeed> BUILDING_SEEDS = List.of(
            new BuildingSeed("IT융합관", "경북 구미시 대학로 61", 36.1462, 128.3945),
            new BuildingSeed("학생회관", "경북 구미시 대학로 61", 36.1470, 128.3950),
            new BuildingSeed("도서관", "경북 구미시 대학로 61", 36.1455, 128.3938)
    );

    private static final List<RoomSeed> ROOM_SEEDS = List.of(
            new RoomSeed("IT융합관", 1, "101호", 60, EnumSet.of(Facility.PROJECTOR, Facility.WIFI, Facility.OUTLET, Facility.AIR_CONDITIONER)),
            new RoomSeed("IT융합관", 1, "102호", 40, EnumSet.of(Facility.WHITEBOARD, Facility.WIFI, Facility.OUTLET)),
            new RoomSeed("IT융합관", 2, "201호", 30, EnumSet.of(Facility.PROJECTOR, Facility.ELECTRONIC_BOARD, Facility.WIFI, Facility.HDMI)),
            new RoomSeed("IT융합관", 2, "202호", 20, EnumSet.of(Facility.WHITEBOARD, Facility.WIFI)),
            new RoomSeed("IT융합관", 3, "301호", 40, EnumSet.of(Facility.PROJECTOR, Facility.WIFI, Facility.OUTLET, Facility.MICROPHONE, Facility.SPEAKER)),
            new RoomSeed("IT융합관", 3, "302호", 20, EnumSet.of(Facility.WHITEBOARD, Facility.WIFI, Facility.OUTLET)),
            new RoomSeed("IT융합관", 4, "401호", 50, EnumSet.of(Facility.PROJECTOR, Facility.VIDEO_CONFERENCE, Facility.WIFI, Facility.HDMI, Facility.MICROPHONE, Facility.SPEAKER)),
            new RoomSeed("IT융합관", 4, "402호", 15, EnumSet.of(Facility.COMPUTER, Facility.WIFI, Facility.OUTLET)),
            new RoomSeed("학생회관", 1, "101호", 80, EnumSet.of(Facility.PROJECTOR, Facility.MICROPHONE, Facility.SPEAKER, Facility.WIFI, Facility.AIR_CONDITIONER)),
            new RoomSeed("학생회관", 1, "102호", 30, EnumSet.of(Facility.WHITEBOARD, Facility.WIFI)),
            new RoomSeed("학생회관", 2, "201호", 25, EnumSet.of(Facility.PROJECTOR, Facility.WIFI, Facility.OUTLET)),
            new RoomSeed("학생회관", 2, "202호", 25, EnumSet.of(Facility.WHITEBOARD, Facility.OUTLET)),
            new RoomSeed("학생회관", 3, "301호", 15, EnumSet.of(Facility.COMPUTER, Facility.WIFI, Facility.HDMI)),
            new RoomSeed("학생회관", 3, "302호", 20, EnumSet.of(Facility.PROJECTOR, Facility.WHITEBOARD, Facility.WIFI)),
            new RoomSeed("도서관", 1, "스터디룸1", 6, EnumSet.of(Facility.WHITEBOARD, Facility.WIFI, Facility.OUTLET)),
            new RoomSeed("도서관", 1, "스터디룸2", 6, EnumSet.of(Facility.WHITEBOARD, Facility.WIFI, Facility.OUTLET)),
            new RoomSeed("도서관", 1, "스터디룸3", 8, EnumSet.of(Facility.WHITEBOARD, Facility.WIFI, Facility.OUTLET, Facility.HDMI)),
            new RoomSeed("도서관", 2, "세미나실1", 12, EnumSet.of(Facility.PROJECTOR, Facility.WIFI, Facility.OUTLET, Facility.HDMI)),
            new RoomSeed("도서관", 2, "세미나실2", 12, EnumSet.of(Facility.PROJECTOR, Facility.WHITEBOARD, Facility.WIFI, Facility.OUTLET)),
            new RoomSeed("도서관", 3, "그룹스터디룸", 10, EnumSet.of(Facility.COMPUTER, Facility.WIFI, Facility.OUTLET, Facility.AIR_CONDITIONER))
    );

    @Override
    public void run(ApplicationArguments args) {
        if (buildingRepository.count() > 0) return;

        Map<String, Building> buildings = BUILDING_SEEDS.stream()
                .map(seed -> Building.builder().name(seed.name()).address(seed.address())
                        .latitude(seed.lat()).longitude(seed.lng()).build())
                .map(buildingRepository::save)
                .collect(Collectors.toMap(Building::getName, b -> b));

        for (RoomSeed seed : ROOM_SEEDS) {
            Room room = Room.builder()
                    .building(buildings.get(seed.buildingName()))
                    .floor(seed.floor())
                    .roomNumber(seed.roomNumber())
                    .capacity(seed.capacity())
                    .usageRule("음식물 반입 금지, 사용 후 원상복구 부탁드립니다.")
                    .facilities(seed.facilities())
                    .build();
            Room saved = roomRepository.save(room);
            createImages(saved);
        }
    }

    private void createImages(Room room) {
        int imageCount = 1 + RANDOM.nextInt(3);
        for (int i = 0; i < imageCount; i++) {
            String imageSeed = room.getId() + "-" + i;
            roomImageRepository.save(RoomImage.builder()
                    .room(room)
                    .imageUrl("https://picsum.photos/seed/" + imageSeed + "/800/600")
                    .sortOrder(i)
                    .build());
        }
    }
}
