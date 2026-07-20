package com.campuson.backend.room.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", nullable = false)
    private Building building;

    @Column(nullable = false)
    private int floor;

    @Column(nullable = false)
    private String roomNumber; // "301호"

    @Column(nullable = false)
    private int capacity;

    @Lob
    private String usageRule; // 이용수칙

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "room_facilities", joinColumns = @JoinColumn(name = "room_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "facility")
    private Set<Facility> facilities = new HashSet<>();

    @Builder
    public Room(Building building, int floor, String roomNumber, int capacity, String usageRule, Set<Facility> facilities) {
        this.building = building;
        this.floor = floor;
        this.roomNumber = roomNumber;
        this.capacity = capacity;
        this.usageRule = usageRule;
        this.facilities = facilities != null ? facilities : new HashSet<>();
    }
}
