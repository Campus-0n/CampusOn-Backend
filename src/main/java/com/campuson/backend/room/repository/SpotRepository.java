package com.campuson.backend.room.repository;

import com.campuson.backend.room.domain.Spot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpotRepository extends JpaRepository<Spot, Long> {
    List<Spot> findByBuilding_Id(Long buildingId);
}
