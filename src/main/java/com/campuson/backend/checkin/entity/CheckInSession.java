package com.campuson.backend.checkin.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 이용 중(CHECKED_IN) 예약의 위치추적 세션 (기능③ 전용).
 *
 * <p>예약과 1:1로 매핑되며 {@code reservationId} 를 PK로 사용한다. 웹앱이 주기적으로 보내는
 * 위치 하트비트를 반영해, 사용자가 강의실 허용 반경 밖으로 처음 벗어난 시각({@code outOfRangeSince})을
 * 추적한다. 반경 안으로 복귀하면 초기화되며, 이탈이 정책상 한계시간 이상 지속되면 예약을 자동취소한다.</p>
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CheckInSession {

    /** 예약 id (1:1). */
    @Id
    private Long reservationId;

    /** 마지막으로 보고된 위도/경도와 그 시각. */
    private Double lastLatitude;
    private Double lastLongitude;
    private LocalDateTime lastReportedAt;

    /** 허용 반경 밖으로 '처음' 벗어난 시각. 반경 안이면 null. */
    private LocalDateTime outOfRangeSince;

    public CheckInSession(Long reservationId) {
        this.reservationId = reservationId;
    }

    /**
     * 위치 보고 1건 반영.
     * 반경 안이면 이탈 타이머를 해제하고, 밖이면 최초 이탈 시각을 기록(이미 있으면 유지)한다.
     */
    public void record(double latitude, double longitude, LocalDateTime now, boolean inRange) {
        this.lastLatitude = latitude;
        this.lastLongitude = longitude;
        this.lastReportedAt = now;
        if (inRange) {
            this.outOfRangeSince = null;
        } else if (this.outOfRangeSince == null) {
            this.outOfRangeSince = now;
        }
    }

    /** 반경 이탈이 limitMinutes 이상 지속되었는지. */
    public boolean isOutOfRangeLongerThan(LocalDateTime now, long limitMinutes) {
        return outOfRangeSince != null
                && !Duration.between(outOfRangeSince, now).minusMinutes(limitMinutes).isNegative();
    }

    /** 자동취소까지 남은 초. 반경 안이면 null. (음수면 0으로 보정) */
    public Long secondsUntilAutoCancel(LocalDateTime now, long limitMinutes) {
        if (outOfRangeSince == null) {
            return null;
        }
        long elapsed = Duration.between(outOfRangeSince, now).getSeconds();
        long remaining = limitMinutes * 60 - elapsed;
        return Math.max(remaining, 0);
    }
}
