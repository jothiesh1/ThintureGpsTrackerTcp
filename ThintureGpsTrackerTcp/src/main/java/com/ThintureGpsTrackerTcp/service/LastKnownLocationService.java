package com.ThintureGpsTrackerTcp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import com.ThintureGpsTrackerTcp.model.DeviceHistory;
import com.ThintureGpsTrackerTcp.model.LastKnownLocationHttps;
import com.ThintureGpsTrackerTcp.repository.DeviceHistoryRepository;
import com.ThintureGpsTrackerTcp.repository.LastKnownLocationHttpsRepository;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Service
public class LastKnownLocationService {

    private static final Logger logger = LoggerFactory.getLogger(LastKnownLocationService.class);

    @Autowired
    private DeviceHistoryRepository deviceHistoryRepository;

    @Autowired
    private LastKnownLocationHttpsRepository lastKnownLocationRepository;

    /**
     * Updates the LastKnownLocationHttps table with the latest data from DeviceHistory.
     *
     * @param deviceId The ID of the device whose location needs to be updated.
     */
    public synchronized void updateLastKnownLocation(String deviceId) {
        logger.info("Started updateLastKnownLocation for deviceId '{}'", deviceId);

        try {
            logger.debug("Fetching latest DeviceHistory for deviceId '{}'", deviceId);
            Optional<DeviceHistory> latestHistoryOpt = deviceHistoryRepository.findLatestHistoryByDeviceId(deviceId);

            if (latestHistoryOpt.isEmpty()) {
                logger.warn("No DeviceHistory found for deviceId '{}'", deviceId);
                throw new RuntimeException("No history available for deviceId " + deviceId);
            }

            DeviceHistory latestHistory = latestHistoryOpt.get();
            if (latestHistory.getLatitude() == null || latestHistory.getLongitude() == null) {
                logger.warn("DeviceHistory for deviceId '{}' contains null latitude/longitude. Skipping update.", deviceId);
                throw new RuntimeException("Invalid latitude/longitude data for deviceId " + deviceId);
            }

            logger.info("Latest DeviceHistory for deviceId '{}': latitude={}, longitude={}, timestamp={}",
                    deviceId, latestHistory.getLatitude(), latestHistory.getLongitude(), latestHistory.getTimestamp());

            LastKnownLocationHttps lastKnownLocation = lastKnownLocationRepository.findByDeviceId(deviceId)
                    .orElseGet(() -> {
                        logger.info("No existing LastKnownLocationHttps entry found. Creating new entry for deviceId '{}'", deviceId);
                        LastKnownLocationHttps newLocation = new LastKnownLocationHttps();
                        newLocation.setDeviceId(deviceId);
                        return newLocation;
                    });

            lastKnownLocation.setLatitude(latestHistory.getLatitude());
            lastKnownLocation.setLongitude(latestHistory.getLongitude());
            lastKnownLocation.setLastUpdated(latestHistory.getTimestamp());

            lastKnownLocationRepository.save(lastKnownLocation);
            logger.info("Successfully updated LastKnownLocationHttps for deviceId '{}'", deviceId);
        } catch (Exception e) {
            logger.error("Error while updating LastKnownLocation for deviceId '{}': {}", deviceId, e.getMessage(), e);
            throw e;
        }

        logger.info("Completed updateLastKnownLocation for deviceId '{}'", deviceId);
    }


    // Fetch a specific device by deviceId
    public Optional<LastKnownLocationHttps> getLastKnownLocationByDeviceId(String deviceId) {
        return lastKnownLocationRepository.findByDeviceId(deviceId);
    }

    // Fetch all devices
    public List<LastKnownLocationHttps> getAllLastKnownLocations() {
        return lastKnownLocationRepository.findAll();
    }

    // Fetch all devices sorted by lastUpdated
    public List<LastKnownLocationHttps> getAllLastKnownLocationsSorted() {
        return lastKnownLocationRepository.findAllByOrderByLastUpdatedDesc();
    }
}
