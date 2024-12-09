package com.ThintureGpsTrackerTcp.controller;
import com.ThintureGpsTrackerTcp.model.Dealer;
import com.ThintureGpsTrackerTcp.repository.DealerRepository;
import com.ThintureGpsTrackerTcp.service.DealerService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DealerLoginController {

    private static final Logger logger = LoggerFactory.getLogger(DealerLoginController.class);

    @Autowired
    private DealerService dealerService;

    @GetMapping("/login")
    public String showLoginPage() {
        logger.info("[CONTROLLER] Rendering login page");
        return "login"; // Renders login.html
    }

    @PostMapping("/login")
    public String handleDealerLogin(@RequestParam("username") String username,
                                    @RequestParam("password") String password,
                                    Model model) {
        logger.info("[CONTROLLER] Login attempt for username: {}", username);

        try {
            boolean isAuthenticated = dealerService.authenticateDealer(username, password);

            if (isAuthenticated) {
                logger.info("[CONTROLLER] Login successful for username: {}", username);
                return "redirect:/dashboard"; // Redirect to the dashboard
            } else {
                logger.warn("[CONTROLLER] Invalid username or password");
                model.addAttribute("error", "Invalid username or password");
                return "login"; // Reload login page with error
            }
        } catch (Exception e) {
            logger.error("[CONTROLLER] Unexpected error during login", e);
            model.addAttribute("error", "An unexpected error occurred. Please try again.");
            return "login";
        }
    }

    @GetMapping("/dashboard")
    public String showDashboard() {
        logger.info("[CONTROLLER] Rendering dashboard page");
        return "dashboard"; // Renders dashboard.html
    }
}
