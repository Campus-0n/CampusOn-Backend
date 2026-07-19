package com.campuson.backend.room.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

/**
 * [스텁] 강의실 엔티티.
 * 기능①(강의실 검색) 담당자의 정식 Room 이 나오기 전까지 예약/인증 개발을 진행하기 위한 임시 엔티티.
 * - 정식 Room 병합 시 이 클래스를 제거하고 교체한다.
 * - GPS 인증(latitude/longitude/allowedRadiusMeters)·QR 인증(qrToken) 필드는
 *   기능③에서 필요하므로 정식 Room 에도 반드시 포함되어야 함(담당자에게 공유).
 */
@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;        // 표시용 이름 (예: "공학관 401")
    private String building;    // 건물
    private String floor;       // 층
    private String roomNumber;  // 호수

    @Column(nullable = false)
    private int capacity;       // 수용 인원

    // --- GPS 인증용 (기능③) ---
    private Double latitude;
    private Double longitude;
    private Double allowedRadiusMeters;

    // --- QR 인증용 (기능③, 강의실 고정 토큰) ---
    private String qrToken;

    @Builder
    public Room(String name, String building, String floor, String roomNumber, int capacity,
                Double latitude, Double longitude, Double allowedRadiusMeters, String qrToken) {
        this.name = name;
        this.building = building;
        this.floor = floor;
        this.roomNumber = roomNumber;
        this.capacity = capacity;
        this.latitude = latitude;
        this.longitude = longitude;
        this.allowedRadiusMeters = allowedRadiusMeters;
        this.qrToken = qrToken;
    }

    /** 목록/상세 표시용 이름. name 이 없으면 건물+호수로 대체. */
    public String displayName() {
        if (name != null && !name.isBlank()) {
            return name;
        }
        String b = building != null ? building : "";
        String r = roomNumber != null ? roomNumber : "";
        return (b + " " + r).trim();
    }
}
