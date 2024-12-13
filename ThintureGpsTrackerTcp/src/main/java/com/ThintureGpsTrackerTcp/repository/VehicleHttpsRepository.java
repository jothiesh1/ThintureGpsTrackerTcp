package com.ThintureGpsTrackerTcp.repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ThintureGpsTrackerTcp.model.VehicleHttps;

public interface VehicleHttpsRepository extends JpaRepository<VehicleHttps, Long> {
    VehicleHttps findByDeviceId(String deviceId);
    List<VehicleHttps> findByDeviceIdContainingOrVehicleNumberContaining(String deviceId, String vehicleNumber);
}
