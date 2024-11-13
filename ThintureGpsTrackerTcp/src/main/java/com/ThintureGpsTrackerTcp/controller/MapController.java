package com.ThintureGpsTrackerTcp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MapController {

    @GetMapping("/map")
    public String showMap() {
        return "map";  // Renders map.html in the resources/templates folder (if using Thymeleaf)
    }
}