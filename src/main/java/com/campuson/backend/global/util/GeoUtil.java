package com.campuson.backend.global.util;

/**
 * 위경도 기반 거리 계산 유틸 (GPS 인증용).
 */
public final class GeoUtil {

    /** 지구 평균 반지름(m). */
    private static final double EARTH_RADIUS_METERS = 6_371_000.0;

    private GeoUtil() {
    }

    /**
     * 두 좌표(위도, 경도) 사이의 대권 거리(최단 경로)를 미터 단위로 반환 (Haversine 공식).
     */
    public static double distanceMeters(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_METERS * c;
    }
}
