package com.ThintureGpsTrackerTcp.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ThintureGpsTrackerTcp.model.DeviceHistory;
import com.ThintureGpsTrackerTcp.repository.DeviceHistoryRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Service
public class DeviceHistoryService {

    private static final Logger logger = LoggerFactory.getLogger(DeviceHistoryService.class);

    @Autowired
    private DeviceHistoryRepository deviceHistoryRepository;

    /**
     * Saves a DeviceHistory object to the database with detailed logging.
     *
     * @param history The DeviceHistory object to save.
     */
    public void saveDeviceHistory(DeviceHistory history) {
        logger.debug("Entering saveDeviceHistory method with history: {}", history);

        try {
            logger.info("Saving DeviceHistory to database: {}", history);
            DeviceHistory savedHistory = deviceHistoryRepository.save(history);
            logger.info("DeviceHistory saved successfully: {}", savedHistory);
        } catch (Exception e) {
            logger.error("Error occurred while saving DeviceHistory: {}", e.getMessage(), e);
        }

        logger.debug("Exiting saveDeviceHistory method");
    }
}
