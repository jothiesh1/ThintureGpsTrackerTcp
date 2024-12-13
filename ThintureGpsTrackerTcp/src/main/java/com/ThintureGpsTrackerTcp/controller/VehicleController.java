package com.ThintureGpsTrackerTcp.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ThintureGpsTrackerTcp.model.VehicleHttps;
import com.ThintureGpsTrackerTcp.service.VehicleHttpsService;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private static final Logger logger = LoggerFactory.getLogger(VehicleController.class);

    @Autowired
    private VehicleHttpsService vehicleHttpsService;

    /**
     * Endpoint to add a new vehicle to the database.
     *
     * @param vehicleHttps The vehicle data to be added.
     * @return A success message if the operation is successful.
     */
    @PostMapping("/add")
    public ResponseEntity<String> addVehicle(@RequestBody VehicleHttps vehicleHttps) {
        logger.info("Received request to add vehicle with Device ID: {}", vehicleHttps.getDeviceId());

        try {
            // Adding vehicle
            logger.debug("Calling VehicleHttpsService.addVehicle() for Device ID: {}", vehicleHttps.getDeviceId());
            vehicleHttpsService.addVehicle(vehicleHttps);
            logger.info("Vehicle with Device ID '{}' added successfully in VehicleHttps table.", vehicleHttps.getDeviceId());

            // Updating history
            logger.debug("Calling VehicleHttpsService.addVehicleAndUpdateHistory() for Device ID: {}", vehicleHttps.getDeviceId());
            vehicleHttpsService.addVehicleAndUpdateHistory(vehicleHttps);
            logger.info("Device history updated successfully for Device ID '{}'.", vehicleHttps.getDeviceId());

            return ResponseEntity.ok("Vehicle added successfully with Device ID: " + vehicleHttps.getDeviceId());
        } catch (Exception e) {
            logger.error("Error adding vehicle with Device ID '{}': {}", vehicleHttps.getDeviceId(), e.getMessage(), e);
            return ResponseEntity.badRequest().body("Failed to add vehicle: " + e.getMessage());
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<VehicleHttps>> searchVehicles(@RequestParam("query") String searchQuery) {
        logger.info("Received search request with query: {}", searchQuery);
        try {
            List<VehicleHttps> vehicles = vehicleHttpsService.searchVehicles(searchQuery);
            return ResponseEntity.ok(vehicles);
        } catch (Exception e) {
            logger.error("Error searching vehicles: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @GetMapping("/all")
    public ResponseEntity<List<VehicleHttps>> getAllVehicles() {
        try {
            List<VehicleHttps> vehicles = vehicleHttpsService.getAllVehicles();
            return ResponseEntity.ok(vehicles);
        } catch (Exception e) {
            logger.error("Error fetching all vehicles: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
    

