package com.campuson.backend.reservation;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 예약/인증 정책 상수. (API 명세 1.3 상수표 기준)
 * 값 조정이 필요하면 여기만 바꾸면 된다.
 */
public final class ReservationPolicy {

    private ReservationPolicy() {
    }

    /** 강의실에 반경 값이 없을 때 사용할 기본 체크인 허용 반경(m). */
    public static final double DEFAULT_CHECKIN_RADIUS_METERS = 50.0;

    /** 이용 중(CHECKED_IN) 강의실 허용 반경 이탈이 이 시간(분) 이상 지속되면 예약 자동취소. */
    public static final int CHECKIN_OUT_OF_RANGE_LIMIT_MINUTES = 10;

    /** 종료 후 다음 예약 제한(버퍼) 시간(분). 충돌검사에 포함. */
    public static final int END_BUFFER_MINUTES = 10;

    /** 시작시각 이후 이 시간(분) 내 미체크인 시 NO_SHOW 처리. */
    public static final int CHECKIN_GRACE_MINUTES = 10;

    /** 시작 X분 전부터 체크인 허용(예정 탭 displayLabel 계산에도 사용). */
    public static final int CHECKIN_PRE_ALLOW_MINUTES = 10;

    /** 연장 최대 횟수 상한. */
    public static final int MAX_EXTENSION_COUNT = 1;

    /** 예약 시간 단위(시간). 예: 2 → 09:00~10:50 (정시 시작, :50 종료). */
    public static final int RESERVATION_UNIT_HOURS = 2;

    /** 연장 시간 단위(시간). */
    public static final int EXTENSION_UNIT_HOURS = 1;

    /** 종료시각 규칙: 정시 기준 이 분(minute)이어야 함 (n시 50분). */
    public static final int REQUIRED_END_MINUTE = 50;

    /** 가용 시간대 조회용 운영 시작/종료 시각. */
    public static final LocalTime OPERATING_START = LocalTime.of(9, 0);
    public static final LocalTime OPERATING_END = LocalTime.of(22, 0);

    /**
     * 두 예약 시간대가 종료 버퍼를 포함해 충돌하는지 검사.
     * 각 예약은 종료 후 END_BUFFER_MINUTES 만큼 다음 예약을 막는다.
     * (운영시간이 자정을 넘지 않는다는 전제 — plusMinutes 래핑 없음)
     */
    public static boolean conflicts(LocalTime aStart, LocalTime aEnd, LocalTime bStart, LocalTime bEnd) {
        return aStart.isBefore(bEnd.plusMinutes(END_BUFFER_MINUTES))
                && bStart.isBefore(aEnd.plusMinutes(END_BUFFER_MINUTES));
    }

    /** 종료시각이 n시 50분 규칙(REQUIRED_END_MINUTE)을 지키는지. */
    public static boolean isValidEndMinute(LocalTime endTime) {
        return endTime.getMinute() == REQUIRED_END_MINUTE;
    }

    /** 시작·종료가 모두 운영시간(OPERATING_START~OPERATING_END) 안에 있는지. */
    public static boolean isWithinOperatingHours(LocalTime startTime, LocalTime endTime) {
        return !startTime.isBefore(OPERATING_START) && !endTime.isAfter(OPERATING_END);
    }

    /** 정시(:00) 시작 + RESERVATION_UNIT_HOURS 단위(종료 :50)인지. 예: 09:00~10:50 */
    public static boolean isValidReservationBlock(LocalTime startTime, LocalTime endTime) {
        return startTime.getMinute() == 0
                && endTime.equals(startTime.plusHours(RESERVATION_UNIT_HOURS - 1).withMinute(REQUIRED_END_MINUTE));
    }

    /** 연장 후 종료시각이 1시간 단위(현재 종료 + EXTENSION_UNIT_HOURS)인지. */
    public static boolean isValidExtension(LocalTime currentEnd, LocalTime newEnd) {
        return newEnd.equals(currentEnd.plusHours(EXTENSION_UNIT_HOURS));
    }

    /**
     * 체크인 가능 시간창 안인지.
     * 허용 구간: [시작 - CHECKIN_PRE_ALLOW_MINUTES, 시작 + CHECKIN_GRACE_MINUTES]
     * (유예를 넘기면 스케줄러가 NO_SHOW 로 전환한다.)
     */
    public static boolean isWithinCheckInWindow(LocalDateTime start, LocalDateTime now) {
        LocalDateTime open = start.minusMinutes(CHECKIN_PRE_ALLOW_MINUTES);
        LocalDateTime close = start.plusMinutes(CHECKIN_GRACE_MINUTES);
        return !now.isBefore(open) && !now.isAfter(close);
    }
}
