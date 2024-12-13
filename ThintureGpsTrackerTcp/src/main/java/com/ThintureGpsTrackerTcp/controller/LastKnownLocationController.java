package com.ThintureGpsTrackerTcp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ThintureGpsTrackerTcp.model.LastKnownLocationHttps;
import com.ThintureGpsTrackerTcp.service.LastKnownLocationService;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@RestController
@RequestMapping("/api/locations")
public class LastKnownLocationController {

    private static final Logger logger = LoggerFactory.getLogger(LastKnownLocationController.class);

    @Autowired
    private LastKnownLocationService lastKnownLocationService;

    /**
     * Update the last known location for a specific device.
     * 
     * @param deviceId The ID of the device.
     * @return ResponseEntity with success or error message.
     */
    @PostMapping("/update/{deviceId}")
    public ResponseEntity<String> updateLastKnownLocation(@PathVariable String deviceId) {
        logger.info("Received request to update last known location for device ID: {}", deviceId);

        try {
            if (deviceId == null || deviceId.trim().isEmpty()) {
                logger.warn("Invalid deviceId provided: {}", deviceId);
                return ResponseEntity.badRequest().body("Invalid device ID");
            }

            lastKnownLocationService.updateLastKnownLocation(deviceId);
            logger.info("Successfully updated last known location for device ID: {}", deviceId);
            return ResponseEntity.ok("Last known location updated successfully for device ID: " + deviceId);
        } catch (Exception e) {
            logger.error("Failed to update last known location for device ID: {}. Error: {}", deviceId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update last known location: " + e.getMessage());
        }
    }

    /**
     * Fetch the last known location for a specific device or all devices.
     * 
     * @param deviceId The ID of the device, or "all" to fetch all devices.
     * @return ResponseEntity with device location(s) or error message.
     */
    @GetMapping("/{deviceId}")
    public ResponseEntity<?> getLastKnownLocation(@PathVariable String deviceId) {
        logger.info("Received request to fetch last known location for device ID: {}", deviceId);

        try {
            if ("all".equalsIgnoreCase(deviceId)) {
                logger.info("Fetching all last known locations");
                return ResponseEntity.ok(lastKnownLocationService.getAllLastKnownLocations());
            }

            return lastKnownLocationService.getLastKnownLocationByDeviceId(deviceId)
                    .map(ResponseEntity::ok)
                    .orElseThrow();
        } catch (Exception e) {
            logger.error("Error fetching last known location for device ID: {}. Error: {}", deviceId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching last known location: " + e.getMessage());
        }
    }

    /**
     * Fetch all last known locations sorted by lastUpdated.
     * 
     * @return ResponseEntity with sorted device locations.
     */
    @GetMapping("/sorted")
    public ResponseEntity<?> getAllLastKnownLocationsSorted() {
        logger.info("Fetching all last known locations sorted by lastUpdated");
        try {
            return ResponseEntity.ok(lastKnownLocationService.getAllLastKnownLocationsSorted());
        } catch (Exception e) {
            logger.error("Error fetching sorted last known locations. Error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching sorted locations: " + e.getMessage());
        }
    }
}


