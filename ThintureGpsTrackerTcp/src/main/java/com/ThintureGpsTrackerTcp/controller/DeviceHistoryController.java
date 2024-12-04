package com.ThintureGpsTrackerTcp.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ThintureGpsTrackerTcp.model.DeviceHistory;
import com.ThintureGpsTrackerTcp.repository.DeviceHistoryRepository;
import com.ThintureGpsTrackerTcp.service.DeviceHistoryService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/device-history")
public class DeviceHistoryController {

    private static final Logger logger = LoggerFactory.getLogger(DeviceHistoryController.class);

    private final DeviceHistoryService deviceHistoryService;

    @Autowired
    public DeviceHistoryController(DeviceHistoryService deviceHistoryService) {
        this.deviceHistoryService = deviceHistoryService;
    }

    /**
     * Endpoint to save a DeviceHistory object.
     *
     * @param deviceHistory The DeviceHistory object to be saved.
     * @return ResponseEntity with the saved DeviceHistory.
     */
    @PostMapping
    public ResponseEntity<DeviceHistory> saveDeviceHistory(@RequestBody DeviceHistory deviceHistory) {
        logger.debug("Entering saveDeviceHistory endpoint with request body: {}", deviceHistory);

        try {
            logger.info("Processing save request for DeviceHistory: {}", deviceHistory);
            deviceHistoryService.saveDeviceHistory(deviceHistory);
            logger.info("DeviceHistory processed and saved successfully: {}", deviceHistory);
            return ResponseEntity.status(HttpStatus.CREATED).body(deviceHistory);
        } catch (Exception e) {
            logger.error("Error while processing DeviceHistory save request: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } finally {
            logger.debug("Exiting saveDeviceHistory endpoint");
        }
    }
}
