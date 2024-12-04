package com.ThintureGpsTrackerTcp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ThintureGpsTrackerTcp.model.DeviceHistory;

@Repository
public interface DeviceHistoryRepository extends JpaRepository<DeviceHistory, Long> {
}










