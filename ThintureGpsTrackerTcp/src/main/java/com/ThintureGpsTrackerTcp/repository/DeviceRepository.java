package com.ThintureGpsTrackerTcp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ThintureGpsTrackerTcp.model.device;

@Repository
public interface DeviceRepository extends JpaRepository<device, Long> {
  
}
