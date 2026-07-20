package com.campuson.backend.room.repository;

import com.campuson.backend.room.domain.Room;
import org.springframework.data.jpa.domain.Specification;

public class RoomSpecification {

    public static Specification<Room> buildingIdEquals(Long buildingId) {
        return (root, query, cb) -> buildingId == null ? null : cb.equal(root.get("building").get("id"), buildingId);
    }

    public static Specification<Room> buildingNameContains(String keyword) {
        return (root, query, cb) -> (keyword == null || keyword.isBlank())
                ? null
                : cb.like(root.get("building").get("name"), "%" + keyword + "%");
    }

    public static Specification<Room> roomNumberContains(String keyword) {
        return (root, query, cb) -> (keyword == null || keyword.isBlank())
                ? null
                : cb.like(root.get("roomNumber"), "%" + keyword + "%");
    }
}
