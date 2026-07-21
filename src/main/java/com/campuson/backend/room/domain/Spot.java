package com.campuson.backend.room.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 거점(빌딩 내 위치점).
 *
 * <p>건물({@link Building})과 강의실({@link Room}) 사이의 계층으로, GPS 좌표를 보유한다.
 * 하나의 건물은 여러 거점을 가질 수 있고(예: 넓은 건물의 구역별 출입구), 각 거점은
 * 여러 강의실을 묶는다. 체크인(QR + GPS)의 위치 판정은 강의실이 속한 거점 좌표를 기준으로 한다.</p>
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Spot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", nullable = false)
    private Building building;

    /** 거점 이름 (예: "정문 거점", "디지털관 거점1"). */
    @Column(nullable = false)
    private String name;

    // --- GPS 인증용 좌표 (체크인 반경 판정 기준) ---
    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    /** 체크인 허용 반경(m). null이면 정책 기본값 사용. */
    private Double allowedRadiusMeters;

    @Builder
    public Spot(Building building, String name, Double latitude, Double longitude, Double allowedRadiusMeters) {
        this.building = building;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.allowedRadiusMeters = allowedRadiusMeters;
    }
}
