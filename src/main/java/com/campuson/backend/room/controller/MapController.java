package com.campuson.backend.room.controller;

import com.campuson.backend.global.response.ResponseBody;
import com.campuson.backend.room.dto.response.BuildingMapResponse;
import com.campuson.backend.room.dto.response.NearestRoomResponse;
import com.campuson.backend.room.service.MapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.campuson.backend.global.response.ResponseUtil.createSuccessResponse;

@RestController
@RequestMapping("/api/map")
@RequiredArgsConstructor
public class MapController {

    private final MapService mapService;

    @GetMapping("/buildings")
    public ResponseEntity<ResponseBody<List<BuildingMapResponse>>> buildings() {
        return ResponseEntity.ok(createSuccessResponse(mapService.getBuildingMapInfo()));
    }

    @GetMapping("/nearest-room")
    public ResponseEntity<ResponseBody<NearestRoomResponse>> nearestRoom(
            @RequestParam double lat,
            @RequestParam double lng
    ) {
        return ResponseEntity.ok(createSuccessResponse(mapService.recommendNearestAvailableRoom(lat, lng)));
    }
}