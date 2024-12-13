package com.ThintureGpsTrackerTcp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ThintureGpsTrackerTcp.model.device;

import com.ThintureGpsTrackerTcp.repository.DeviceRepository;

@Service
public class DeviceService {

    @Autowired
    private DeviceRepository deviceRepository;

  

    // Save a device
    public device saveDevice(device device) {
        return deviceRepository.save(device);
    }

    // Get a device by ID
    public device getDeviceById(Long id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Device not found with ID: " + id));
    }

    

    // Update a device
    public device updateDevice(Long id, device updatedDevice) {
    	device existingDevice = getDeviceById(id);
        existingDevice.setSerialNumber(updatedDevice.getSerialNumber());
        existingDevice.setStatus(updatedDevice.isStatus());
        existingDevice.setInstallationDate(updatedDevice.getInstallationDate());
        return deviceRepository.save(existingDevice);
    }

    // Delete a device
    public void deleteDevice(Long id) {
        deviceRepository.deleteById(id);
    }
}
