package com.campuson.backend.room.domain;

import lombok.Getter;

@Getter
public enum Facility {
    PROJECTOR("프로젝터"),
    ELECTRONIC_BOARD("전자칠판"),
    WHITEBOARD("일반 칠판"),
    OUTLET("콘센트"),
    WIFI("와이파이"),
    COMPUTER("컴퓨터"),
    MICROPHONE("마이크"),
    SPEAKER("스피커"),
    VIDEO_CONFERENCE("화상회의 장비"),
    HDMI("HDMI선"),
    AIR_CONDITIONER("냉난방");

    private final String label;

    Facility(String label) {
        this.label = label;
    }
}
