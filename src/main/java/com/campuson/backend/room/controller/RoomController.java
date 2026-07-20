package com.campuson.backend.room.controller;

import com.campuson.backend.global.response.ResponseBody;
import com.campuson.backend.room.domain.Facility;
import com.campuson.backend.room.dto.request.RoomSearchRequest;
import com.campuson.backend.room.dto.response.RoomDetailResponse;
import com.campuson.backend.room.dto.response.RoomSearchResponse;
import com.campuson.backend.room.service.RoomDetailService;
import com.campuson.backend.room.service.RoomSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static com.campuson.backend.global.response.ResponseUtil.createSuccessResponse;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomSearchService roomSearchService;
    private final RoomDetailService roomDetailService;

    @GetMapping("/search")
    public ResponseEntity<ResponseBody<List<RoomSearchResponse>>> search(
            @RequestParam(required = false) Long buildingId,
            @RequestParam(required = false) String buildingName,
            @RequestParam(required = false) String roomNumber,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) LocalTime startTime,
            @RequestParam(required = false) LocalTime endTime,
            @RequestParam(required = false) List<Facility> facilities,
            @RequestParam(defaultValue = "false") boolean availableOnly
    ) {
        RoomSearchRequest request = new RoomSearchRequest(
                buildingId, buildingName, roomNumber, date, startTime, endTime, facilities, availableOnly
        );
        return ResponseEntity.ok(createSuccessResponse(roomSearchService.search(request)));
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<ResponseBody<RoomDetailResponse>> detail(@PathVariable Long roomId) {
        return ResponseEntity.ok(createSuccessResponse(roomDetailService.getDetail(roomId)));
    }
}
