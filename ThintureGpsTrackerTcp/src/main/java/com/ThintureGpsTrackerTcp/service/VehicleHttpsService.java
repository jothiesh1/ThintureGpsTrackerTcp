package com.ThintureGpsTrackerTcp.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ThintureGpsTrackerTcp.model.DeviceHistory;
import com.ThintureGpsTrackerTcp.model.VehicleHttps;
import com.ThintureGpsTrackerTcp.repository.DeviceHistoryRepository;
import com.ThintureGpsTrackerTcp.repository.VehicleHttpsRepository;

import jakarta.transaction.Transactional;
@Service
public class VehicleHttpsService {

    private static final Logger logger = LoggerFactory.getLogger(VehicleHttpsService.class);

    @Autowired
    private VehicleHttpsRepository vehicleHttpsRepository;

    @Autowired
    private DeviceHistoryRepository deviceHistoryRepository;

    /**
     * Adds a new vehicle entry in the VehicleHttps table. Throws an exception if
     * the deviceId already exists.
     */
    public void addVehicle(VehicleHttps vehicleHttps) {
        logger.info("Checking if a vehicle with deviceId '{}' already exists.", vehicleHttps.getDeviceId());

        // Check if a vehicle with the same deviceId already exists
        VehicleHttps existingVehicle = vehicleHttpsRepository.findByDeviceId(vehicleHttps.getDeviceId());
        if (existingVehicle != null) {
            logger.error("Vehicle with Device ID '{}' already exists.", vehicleHttps.getDeviceId());
            throw new RuntimeException("Vehicle with Device ID/IMEI " + vehicleHttps.getDeviceId() + " already exists.");
        }

        // Save the new vehicle
        logger.info("Saving new vehicle with deviceId '{}'.", vehicleHttps.getDeviceId());
        vehicleHttpsRepository.save(vehicleHttps);
        logger.info("Vehicle with deviceId '{}' saved successfully.", vehicleHttps.getDeviceId());
    }

    /**
     * Adds or updates a vehicle in the VehicleHttps table and ensures the
     * DeviceHistory table is updated with the latest information.
     */
    @Transactional
    public void addVehicleAndUpdateHistory(VehicleHttps vehicleHttps) {
        logger.info("Started addVehicleAndUpdateHistory for deviceId '{}'", vehicleHttps.getDeviceId());

        try {
            // Step 1: Save VehicleHttps entity
            logger.debug("Attempting to save VehicleHttps for deviceId '{}'", vehicleHttps.getDeviceId());
            VehicleHttps savedVehicle = vehicleHttpsRepository.save(vehicleHttps);
            logger.info("VehicleHttps saved successfully with deviceId '{}'", savedVehicle.getDeviceId());

            // Step 2: Create a new DeviceHistory entry
            logger.debug("Creating a new DeviceHistory entry for deviceId '{}'", savedVehicle.getDeviceId());
            DeviceHistory deviceHistory = new DeviceHistory();
            deviceHistory.setDeviceId(savedVehicle.getDeviceId());
            deviceHistory.setLatitude(0.0); // Default value
            deviceHistory.setLongitude(0.0); // Default value
            deviceHistory.setTimestamp(LocalDateTime.now()); // Current timestamp
            deviceHistory.setSpeed(0); // Default speed
            deviceHistory.setIgnitionState("OFF"); // Default ignition state

            // Step 3: Save the DeviceHistory entry
            logger.debug("Attempting to save DeviceHistory for deviceId '{}'", savedVehicle.getDeviceId());
            DeviceHistory savedHistory = deviceHistoryRepository.save(deviceHistory);
            logger.info("DeviceHistory saved successfully with id '{}' for deviceId '{}'", savedHistory.getId(), savedHistory.getDeviceId());

        } catch (Exception e) {
            logger.error("Error occurred while saving vehicle or updating device history for deviceId '{}': {}", vehicleHttps.getDeviceId(), e.getMessage(), e);
            throw e;
        }

        logger.info("Completed addVehicleAndUpdateHistory for deviceId '{}'", vehicleHttps.getDeviceId());
    }
    /**
     * Search for vehicles by Device ID or Vehicle Number.
     *
     * @param query The search term (Device ID or Vehicle Number).
     * @return A list of matching vehicles.
     */
    public List<VehicleHttps> searchVehicles(String query) {
        return vehicleHttpsRepository.findByDeviceIdContainingOrVehicleNumberContaining(query, query);
    }
    public List<VehicleHttps> getAllVehicles() {
        return vehicleHttpsRepository.findAll();
    }

}
