package com.ThintureGpsTrackerTcp.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ThintureGpsTrackerTcp.model.LastKnownLocationHttps;

	public interface LastKnownLocationHttpsRepository extends JpaRepository<LastKnownLocationHttps, Long> {

	    // Fetch a specific device by deviceId
	    Optional<LastKnownLocationHttps> findByDeviceId(String deviceId);

	    // Fetch all devices
	    List<LastKnownLocationHttps> findAll();

	    // Fetch all devices sorted by lastUpdated in descending order
	    List<LastKnownLocationHttps> findAllByOrderByLastUpdatedDesc();
	}

