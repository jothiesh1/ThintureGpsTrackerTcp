package com.ThintureGpsTrackerTcp.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ThintureGpsTrackerTcp.model.DeviceHistory;
import com.ThintureGpsTrackerTcp.repository.DeviceHistoryRepository;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;













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



































/*






@Service
public class DeviceHistoryService {

    private static final Logger logger = LoggerFactory.getLogger(DeviceHistoryService.class);

    @Autowired
    private DeviceHistoryRepository deviceHistoryRepository;

    /**
     * Saves a DeviceHistory object to the database with detailed logging.
     *
     * @param history The DeviceHistory object to save.
     
    public void saveDeviceHistory(DeviceHistory history) {
        logger.debug("Entering saveDeviceHistory method with history: {}", history);

        if (history == null) {
            logger.warn("DeviceHistory object is null. Skipping save operation.");
            return;
        }

        try {
            // Validate critical fields
            if (history.getDeviceId() == null) {
                logger.warn("Device ID is null in DeviceHistory object: {}", history);
                return;
            }
            if (history.getTimestamp() == null) {
                logger.warn("Timestamp is null in DeviceHistory object: {}", history);
                return;
            }

            // Check for duplicate records
            logger.debug("Checking for duplicate records for Device ID: {} and Timestamp: {}", 
                         history.getDeviceId(), history.getTimestamp());
            Optional<DeviceHistory> existingHistory = deviceHistoryRepository.findFirstByDeviceIdOrderByTimestampDesc(history.getDeviceId());

            if (existingHistory.isPresent() && existingHistory.get().getTimestamp().equals(history.getTimestamp())) {
                logger.info("Duplicate entry detected for Device ID: {} with timestamp: {}. Skipping save.", 
                             history.getDeviceId(), history.getTimestamp());
                return;
            }

            // Save the new record
            logger.info("Saving DeviceHistory to the database: {}", history);
            DeviceHistory savedHistory = deviceHistoryRepository.save(history);
            logger.info("DeviceHistory saved successfully with ID: {}", savedHistory.getId());
        } catch (Exception e) {
            logger.error("Error occurred while saving DeviceHistory: {}", e.getMessage(), e);
        } finally {
            logger.debug("Exiting saveDeviceHistory method");
        }
    }


    /**
     * Fetches the latest DeviceHistory for a given deviceId.
     *
     * @param deviceId The device ID to look up.
     * @return The latest DeviceHistory entry.
    
    public DeviceHistory getLatestHistoryByDeviceId(String deviceId) {
        logger.debug("Entering getLatestHistoryByDeviceId with deviceId: {}", deviceId);

        try {
            return deviceHistoryRepository.findLatestHistoryByDeviceId(deviceId)
                    .orElseThrow(() -> {
                        logger.warn("No DeviceHistory found for deviceId: {}", deviceId);
                        return new RuntimeException("No DeviceHistory found for deviceId: " + deviceId);
                    });
        } catch (Exception e) {
            logger.error("Error occurred while fetching latest DeviceHistory for deviceId: {}. Message: {}", 
                         deviceId, e.getMessage(), e);
            throw e;
        } finally {
            logger.debug("Exiting getLatestHistoryByDeviceId method");
        }
    }

    /**
     * Updates an existing DeviceHistory entry based on the given ID and new details.
     *
     * @param id The ID of the DeviceHistory entry to update.
     * @param updatedHistory The new details to update.
     * @return The updated DeviceHistory object.
     
    public DeviceHistory updateDeviceHistory(Long id, DeviceHistory updatedHistory) {
        logger.debug("Entering updateDeviceHistory with ID: {} and updated details: {}", id, updatedHistory);

        try {
            // Fetch the existing entry
            DeviceHistory existingHistory = deviceHistoryRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("No DeviceHistory found with ID: {}", id);
                        return new RuntimeException("DeviceHistory not found with ID: " + id);
                    });

            // Update the fields
            existingHistory.setDeviceId(updatedHistory.getDeviceId());
            existingHistory.setLatitude(updatedHistory.getLatitude());
            existingHistory.setLongitude(updatedHistory.getLongitude());
            existingHistory.setTimestamp(updatedHistory.getTimestamp());
            existingHistory.setSpeed(updatedHistory.getSpeed());
            existingHistory.setIgnitionState(updatedHistory.getIgnitionState());

            // Save the updated entry
            DeviceHistory savedHistory = deviceHistoryRepository.save(existingHistory);
            logger.info("DeviceHistory updated successfully with ID: {}", savedHistory.getId());
            return savedHistory;
        } catch (Exception e) {
            logger.error("Error occurred while updating DeviceHistory with ID: {}. Message: {}", id, e.getMessage(), e);
            throw e;
        } finally {
            logger.debug("Exiting updateDeviceHistory method");
        }
    }

    /**
     * Deletes a DeviceHistory entry by its ID.
     *
     * @param id The ID of the DeviceHistory to delete.
     
    public void deleteDeviceHistory(Long id) {
        logger.debug("Entering deleteDeviceHistory with ID: {}", id);

        try {
            deviceHistoryRepository.deleteById(id);
            logger.info("DeviceHistory with ID: {} deleted successfully.", id);
        } catch (Exception e) {
            logger.error("Error occurred while deleting DeviceHistory with ID: {}. Message: {}", id, e.getMessage(), e);
            throw e;
        } finally {
            logger.debug("Exiting deleteDeviceHistory method");
        }
    }
}
*/