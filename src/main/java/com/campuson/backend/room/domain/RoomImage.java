package com.campuson.backend.room.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "room_images")
public class RoomImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private int sortOrder; // 0 = 대표 이미지

    @Builder
    public RoomImage(Room room, String imageUrl, int sortOrder) {
        this.room = room;
        this.imageUrl = imageUrl;
        this.sortOrder = sortOrder;
    }
}
