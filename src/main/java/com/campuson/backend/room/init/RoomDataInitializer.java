package com.campuson.backend.room.init;

import com.campuson.backend.room.domain.*;
import com.campuson.backend.room.repository.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 강의실 더미데이터 시더. 건물 → 거점 → 강의실 3계층으로 심는다.
 *
 * <ul>
 *   <li><b>건물 시드</b>: 이름·주소. (지도용 좌표는 대표 거점 좌표로 채운다)</li>
 *   <li><b>거점 시드</b>: 건물 내 위치점(GPS 좌표·허용반경). 체크인 반경 판정의 기준.</li>
 *   <li><b>강의실 시드</b>: 층·호수·정원·편의시설. 소속 거점의 좌표와 자체 QR 토큰으로 체크인.</li>
 * </ul>
 *
 * <p>{@code app.seed-data.enabled=true} 일 때만 동작하며, 건물이 이미 존재하면 건너뛴다.</p>
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.seed-data.enabled", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
public class RoomDataInitializer implements ApplicationRunner {

    private final BuildingRepository buildingRepository;
    private final SpotRepository spotRepository;
    private final RoomRepository roomRepository;
    private final RoomImageRepository roomImageRepository;

    private static final Random RANDOM = new Random();
    private static final String ADDRESS = "경북 구미시 대학로 61";
    private static final double CHECKIN_RADIUS_METERS = 50.0;
    private static final String USAGE_RULE = "음식물 반입 금지, 사용 후 원상복구 부탁드립니다.";

    private record BuildingSeed(String name) {}
    private record SpotSeed(String buildingName, String code, String spotName, double lat, double lng) {}
    private record RoomSeed(String spotCode, int floor, String roomNumber, int capacity, Set<Facility> facilities) {}

    private static final List<BuildingSeed> BUILDING_SEEDS = List.of(
            new BuildingSeed("IT융합관"),
            new BuildingSeed("학생회관"),
            new BuildingSeed("도서관"),
            new BuildingSeed("디지털관")   // 신규 건물
    );

    // 거점(빌딩 내 위치점) — 기존 3건물은 각 1거점(옛 건물 좌표), 디지털관은 지도 좌표 3개로 3거점
    private static final List<SpotSeed> SPOT_SEEDS = List.of(
            new SpotSeed("IT융합관", "IT",  "IT융합관 거점",  36.1462,    128.3945),
            new SpotSeed("학생회관", "SH",  "학생회관 거점",  36.1470,    128.3950),
            new SpotSeed("도서관",   "LIB", "도서관 거점",    36.1455,    128.3938),
            new SpotSeed("디지털관", "D1",  "디지털관 거점1", 36.146088,  128.392359),
            new SpotSeed("디지털관", "D2",  "디지털관 거점2", 36.145912,  128.392797),
            new SpotSeed("디지털관", "D3",  "디지털관 거점3", 36.145588,  128.392484)
    );

    // 기존 강의실 — 각 건물의 단일 거점에 배속
    private static final List<RoomSeed> ROOM_SEEDS = List.of(
            new RoomSeed("IT", 1, "101호", 60, EnumSet.of(Facility.PROJECTOR, Facility.WIFI, Facility.OUTLET, Facility.AIR_CONDITIONER)),
            new RoomSeed("IT", 1, "102호", 40, EnumSet.of(Facility.WHITEBOARD, Facility.WIFI, Facility.OUTLET)),
            new RoomSeed("IT", 2, "201호", 30, EnumSet.of(Facility.PROJECTOR, Facility.ELECTRONIC_BOARD, Facility.WIFI, Facility.HDMI)),
            new RoomSeed("IT", 2, "202호", 20, EnumSet.of(Facility.WHITEBOARD, Facility.WIFI)),
            new RoomSeed("IT", 3, "301호", 40, EnumSet.of(Facility.PROJECTOR, Facility.WIFI, Facility.OUTLET, Facility.MICROPHONE, Facility.SPEAKER)),
            new RoomSeed("IT", 3, "302호", 20, EnumSet.of(Facility.WHITEBOARD, Facility.WIFI, Facility.OUTLET)),
            new RoomSeed("IT", 4, "401호", 50, EnumSet.of(Facility.PROJECTOR, Facility.VIDEO_CONFERENCE, Facility.WIFI, Facility.HDMI, Facility.MICROPHONE, Facility.SPEAKER)),
            new RoomSeed("IT", 4, "402호", 15, EnumSet.of(Facility.COMPUTER, Facility.WIFI, Facility.OUTLET)),
            new RoomSeed("SH", 1, "101호", 80, EnumSet.of(Facility.PROJECTOR, Facility.MICROPHONE, Facility.SPEAKER, Facility.WIFI, Facility.AIR_CONDITIONER)),
            new RoomSeed("SH", 1, "102호", 30, EnumSet.of(Facility.WHITEBOARD, Facility.WIFI)),
            new RoomSeed("SH", 2, "201호", 25, EnumSet.of(Facility.PROJECTOR, Facility.WIFI, Facility.OUTLET)),
            new RoomSeed("SH", 2, "202호", 25, EnumSet.of(Facility.WHITEBOARD, Facility.OUTLET)),
            new RoomSeed("SH", 3, "301호", 15, EnumSet.of(Facility.COMPUTER, Facility.WIFI, Facility.HDMI)),
            new RoomSeed("SH", 3, "302호", 20, EnumSet.of(Facility.PROJECTOR, Facility.WHITEBOARD, Facility.WIFI)),
            new RoomSeed("LIB", 1, "스터디룸1", 6, EnumSet.of(Facility.WHITEBOARD, Facility.WIFI, Facility.OUTLET)),
            new RoomSeed("LIB", 1, "스터디룸2", 6, EnumSet.of(Facility.WHITEBOARD, Facility.WIFI, Facility.OUTLET)),
            new RoomSeed("LIB", 1, "스터디룸3", 8, EnumSet.of(Facility.WHITEBOARD, Facility.WIFI, Facility.OUTLET, Facility.HDMI)),
            new RoomSeed("LIB", 2, "세미나실1", 12, EnumSet.of(Facility.PROJECTOR, Facility.WIFI, Facility.OUTLET, Facility.HDMI)),
            new RoomSeed("LIB", 2, "세미나실2", 12, EnumSet.of(Facility.PROJECTOR, Facility.WHITEBOARD, Facility.WIFI, Facility.OUTLET)),
            new RoomSeed("LIB", 3, "그룹스터디룸", 10, EnumSet.of(Facility.COMPUTER, Facility.WIFI, Facility.OUTLET, Facility.AIR_CONDITIONER))
    );

    // 디지털관 각 거점에 10개씩 생성할 때 돌려쓸 편의시설 프리셋
    private static final List<Set<Facility>> DIGITAL_FACILITY_PRESETS = List.of(
            EnumSet.of(Facility.PROJECTOR, Facility.WIFI, Facility.OUTLET, Facility.AIR_CONDITIONER),
            EnumSet.of(Facility.WHITEBOARD, Facility.WIFI, Facility.OUTLET),
            EnumSet.of(Facility.ELECTRONIC_BOARD, Facility.WIFI, Facility.HDMI, Facility.SPEAKER),
            EnumSet.of(Facility.COMPUTER, Facility.WIFI, Facility.OUTLET)
    );
    private static final List<String> DIGITAL_SPOT_CODES = List.of("D1", "D2", "D3");
    private static final int DIGITAL_ROOMS_PER_SPOT = 10;

    @Override
    public void run(ApplicationArguments args) {
        if (buildingRepository.count() > 0) return;

        // 1) 건물: 지도용 좌표는 그 건물의 대표(첫) 거점 좌표로 채운다
        Map<String, double[]> representativeCoord = new LinkedHashMap<>();
        for (SpotSeed s : SPOT_SEEDS) {
            representativeCoord.putIfAbsent(s.buildingName(), new double[]{s.lat(), s.lng()});
        }
        Map<String, Building> buildings = new HashMap<>();
        for (BuildingSeed b : BUILDING_SEEDS) {
            double[] c = representativeCoord.getOrDefault(b.name(), new double[]{0, 0});
            buildings.put(b.name(), buildingRepository.save(Building.builder()
                    .name(b.name()).address(ADDRESS)
                    .latitude(c[0]).longitude(c[1]).build()));
        }

        // 2) 거점
        Map<String, Spot> spots = new HashMap<>();
        for (SpotSeed s : SPOT_SEEDS) {
            spots.put(s.code(), spotRepository.save(Spot.builder()
                    .building(buildings.get(s.buildingName()))
                    .name(s.spotName())
                    .latitude(s.lat()).longitude(s.lng())
                    .allowedRadiusMeters(CHECKIN_RADIUS_METERS)
                    .build()));
        }

        // 3) 강의실 — 기존
        int roomCount = 0;
        for (RoomSeed r : ROOM_SEEDS) {
            createRoom(spots.get(r.spotCode()), r.spotCode(), r.floor(), r.roomNumber(), r.capacity(), r.facilities());
            roomCount++;
        }
        // 3) 강의실 — 디지털관: 거점당 10개
        for (String code : DIGITAL_SPOT_CODES) {
            Spot spot = spots.get(code);
            for (int i = 1; i <= DIGITAL_ROOMS_PER_SPOT; i++) {
                int floor = (i - 1) / 2 + 1;                       // 1~5층, 층당 2개
                int number = floor * 100 + ((i - 1) % 2 + 1);      // 101,102,201,...
                int capacity = 10 + (i % 5) * 6;                   // 10~34
                Set<Facility> facilities = DIGITAL_FACILITY_PRESETS.get((i - 1) % DIGITAL_FACILITY_PRESETS.size());
                createRoom(spot, code, floor, number + "호", capacity, facilities);
                roomCount++;
            }
        }

        log.info("[시더] 건물 {}개 / 거점 {}개 / 강의실 {}개 생성 완료",
                buildings.size(), spots.size(), roomCount);
    }

    private void createRoom(Spot spot, String spotCode, int floor, String roomNumber,
                            int capacity, Set<Facility> facilities) {
        Room room = roomRepository.save(Room.builder()
                .building(spot.getBuilding())
                .spot(spot)
                .qrToken("QR-" + spotCode + "-" + roomNumber.replace("호", ""))
                .floor(floor)
                .roomNumber(roomNumber)
                .capacity(capacity)
                .usageRule(USAGE_RULE)
                .facilities(facilities)
                .build());
        createImages(room);
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
