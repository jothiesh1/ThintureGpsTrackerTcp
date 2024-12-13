package com.ThintureGpsTrackerTcp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ThintureGpsTrackerTcp.model.DeviceHistory;

@Repository
public interface DeviceHistoryRepository extends JpaRepository<DeviceHistory, Long> {
	
	
	 Optional<DeviceHistory> findFirstByDeviceIdOrderByTimestampDesc(String deviceId);
	 // @Query("SELECT d FROM DeviceHistory d WHERE d.deviceId = :deviceId ORDER BY d.timestamp DESC")
	  //  Optional<DeviceHistory> findLatestHistoryByDeviceId(@Param("deviceId") String deviceId);
	    @Query("SELECT d FROM DeviceHistory d WHERE d.deviceId = :deviceId ORDER BY d.timestamp DESC")
	    Optional<DeviceHistory> findLatestHistoryByDeviceId(@Param("deviceId") String deviceId);
}










