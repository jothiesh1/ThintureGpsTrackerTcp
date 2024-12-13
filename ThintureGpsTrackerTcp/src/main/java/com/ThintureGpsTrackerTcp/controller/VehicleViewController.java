package com.ThintureGpsTrackerTcp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import ch.qos.logback.core.model.Model;

@Controller
public class VehicleViewController {

    @GetMapping("/vehicle")
    public String showVehiclePage(Model model) {
        // Add any attributes to the model if necessary
        return "vehicle"; // This should match the vehicle.html file name in /templates folder
    }
}
