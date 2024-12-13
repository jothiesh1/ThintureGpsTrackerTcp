package com.ThintureGpsTrackerTcp.controller;

import com.ThintureGpsTrackerTcp.model.Dealer;
import com.ThintureGpsTrackerTcp.service.DealerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dealers")
public class DealerController {

    private static final Logger logger = LoggerFactory.getLogger(DealerController.class);

    @Autowired
    private DealerService dealerService;

    @GetMapping("/autocomplete")
    public ResponseEntity<List<Dealer>> getDealers(@RequestParam String name) {
        logger.info("Received request for dealer autocomplete with name: {}", name);

        List<Dealer> dealers = dealerService.getDealers(name);
        logger.info("Found {} dealers for name: {}", dealers.size(), name);

        return ResponseEntity.ok(dealers);
    }

    @PostMapping("/add-single-serial")
    public ResponseEntity<String> addSingleSerial(@RequestBody Map<String, String> payload) {
        logger.info("Received request to add single serial number with payload: {}", payload);

        String dealerName = payload.get("dealerName");
        String serialNumber = payload.get("serialNumber");

        logger.debug("Dealer Name: {}, Serial Number: {}", dealerName, serialNumber);

        try {
            dealerService.addSingleSerialNumber(dealerName, serialNumber);
            logger.info("Successfully added serial number: {} to dealer: {}", serialNumber, dealerName);
            return ResponseEntity.ok("Serial number added successfully.");
        } catch (Exception e) {
            logger.error("Error adding serial number: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to add serial number.");
        }
    }

    @PostMapping("/add-serial-range")
    public ResponseEntity<String> addSerialRange(
            @RequestParam String dealerName,
            @RequestParam int start,
            @RequestParam int end,
            @RequestParam(required = false) Integer removedNumber) {
        logger.info("Received request to add serial range for dealer: {}, start: {}, end: {}, removed: {}",
                dealerName, start, end, removedNumber);

        try {
            dealerService.addSerialNumbersInRange(dealerName, start, end, removedNumber);
            logger.info("Successfully added serial range for dealer: {}", dealerName);
            return ResponseEntity.ok("Serial numbers range added successfully.");
        } catch (Exception e) {
            logger.error("Error adding serial range: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to add serial range.");
        }
    }
}
