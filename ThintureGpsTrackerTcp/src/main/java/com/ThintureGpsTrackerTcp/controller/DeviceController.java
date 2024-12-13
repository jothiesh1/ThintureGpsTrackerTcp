package com.ThintureGpsTrackerTcp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ThintureGpsTrackerTcp.model.device;
import com.ThintureGpsTrackerTcp.service.DeviceService;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    @Autowired
    private DeviceService deviceService;

    @PostMapping("/add")
    public ResponseEntity<device> addDevice(@RequestBody device device) {
    	device savedDevice = deviceService.saveDevice(device);
        return ResponseEntity.ok(savedDevice);
    }

    @GetMapping("/{id}")
    public ResponseEntity<device> getDeviceById(@PathVariable Long id) {
    	device device = deviceService.getDeviceById(id);
        return ResponseEntity.ok(device);
    }

   

    @PutMapping("/{id}")
    public ResponseEntity<device> updateDevice(@PathVariable Long id, @RequestBody device device) {
    	device updatedDevice = deviceService.updateDevice(id, device);
        return ResponseEntity.ok(updatedDevice);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDevice(@PathVariable Long id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.ok("Device deleted successfully.");
    }
}
