package com.campuson.backend.room.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "room_schedules")
public class RoomSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    private String title; // "전공수업", "동아리 예약" 등 (선택)

    @Builder
    public RoomSchedule(Room room, LocalDateTime startTime, LocalDateTime endTime, String title) {
        this.room = room;
        this.startTime = startTime;
        this.endTime = endTime;
        this.title = title;
    }
}
