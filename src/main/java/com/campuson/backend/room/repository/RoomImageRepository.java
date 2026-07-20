package com.campuson.backend.room.repository;

import com.campuson.backend.room.domain.RoomImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomImageRepository extends JpaRepository<RoomImage, Long> {
    List<RoomImage> findByRoom_IdOrderBySortOrderAsc(Long roomId);
    Optional<RoomImage> findFirstByRoom_IdOrderBySortOrderAsc(Long roomId);
}
