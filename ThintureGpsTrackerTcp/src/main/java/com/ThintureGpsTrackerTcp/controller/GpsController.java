package com.ThintureGpsTrackerTcp.controller;


import com.ThintureGpsTrackerTcp.DTO.DeviceLocation;
import com.ThintureGpsTrackerTcp.handler.GPSWebSocketHandler;
import com.ThintureGpsTrackerTcp.model.GpsData;
import com.ThintureGpsTrackerTcp.service.TcpServerService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.ThintureGpsTrackerTcp.service.TcpServerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;
@RestController
public class GpsController {

    private static final Logger logger = LoggerFactory.getLogger(GpsController.class);

    private Map<String, DeviceLocation> deviceLocations = new ConcurrentHashMap<>();

    @Autowired
    private GPSWebSocketHandler gpsWebSocketHandler;

    // Endpoint to get all devices' latest locations
    @GetMapping("/api/latest-locations")
    public ResponseEntity<Map<String, DeviceLocation>> getLatestLocations() {
        logger.info("Providing latest locations for all devices.");
        return ResponseEntity.ok(deviceLocations);
    }

    // Method to update location for a specific device
    public void updateLocation(String deviceID, double latitude, double longitude) {
        deviceLocations.put(deviceID, new DeviceLocation(latitude, longitude));
        gpsWebSocketHandler.broadcastLocation(deviceID, latitude, longitude);  // Broadcast per device
        logger.debug("Updated location for device {}: Latitude = {}, Longitude = {}", deviceID, latitude, longitude);
    }
}
