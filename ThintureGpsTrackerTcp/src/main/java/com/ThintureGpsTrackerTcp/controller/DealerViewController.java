package com.ThintureGpsTrackerTcp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class DealerViewController {

    @GetMapping("/device-to-dealer")
    public String showDeviceToDealerPage(Model model) {
        model.addAttribute("title", "Device to Dealer Mapping");
        return "device_to_dealer"; // Ensure the file exists in the templates directory
    }
}
