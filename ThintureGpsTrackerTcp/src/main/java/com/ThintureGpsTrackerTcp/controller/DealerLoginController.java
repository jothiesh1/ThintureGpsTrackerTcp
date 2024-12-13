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
    public String handleLogin(@RequestParam("usertype") String usertype,
                              @RequestParam("username") String username,
                              @RequestParam("password") String password,
                              Model model) {
        logger.info("[CONTROLLER] Login attempt for username: {}, usertype: {}", username, usertype);

        try {
            // Authenticate dealer with usertype
            boolean isAuthenticated = dealerService.authenticateDealer(username, password, usertype);

            if (isAuthenticated) {
                logger.info("[CONTROLLER] Login successful for username: {}, usertype: {}", username, usertype);

                // Redirect to the appropriate dashboard based on usertype
                return "redirect:/dashboard?usertype=" + usertype;
            } else {
                logger.warn("[CONTROLLER] Invalid username, password, or usertype");
                model.addAttribute("error", "Invalid username, password, or user type.");
                return "login"; // Reload login page with error
            }
        } catch (Exception e) {
            logger.error("[CONTROLLER] Unexpected error during login", e);
            model.addAttribute("error", "An unexpected error occurred. Please try again.");
            return "login";
        }
    }

    @GetMapping("/dashboard")
    public String showDashboard(@RequestParam("usertype") String usertype, Model model) {
        logger.info("[CONTROLLER] Rendering dashboard for usertype: {}", usertype);

        // Add usertype to the model for navigation
        model.addAttribute("usertype", usertype);

        // Render different dashboards based on usertype
        switch (usertype.toUpperCase()) {
            case "SUPERADMIN":
                logger.info("[CONTROLLER] Resolving template: dashboard_superadmin");
                return "dashboard_superadmin"; // Renders dashboard_superadmin.html
            case "ADMIN":
                logger.info("[CONTROLLER] Resolving template: dashboard_admin");
                return "dashboard_admin"; // Renders dashboard_admin.html
            case "DEALER":
                logger.info("[CONTROLLER] Resolving template: dashboard_dealer");
                return "dashboard_dealer"; // Renders dashboard_dealer.html
            case "CLIENT":
                logger.info("[CONTROLLER] Resolving template: dashboard_client");
                return "dashboard_client"; // Renders dashboard_client.html
            case "USER":
                logger.info("[CONTROLLER] Resolving template: dashboard_user");
                return "dashboard_user"; // Renders dashboard_user.html
            default:
                logger.warn("[CONTROLLER] Invalid usertype: {}", usertype);
                model.addAttribute("error", "Invalid user type.");
                return "login"; // Redirect back to login for invalid usertype
        }
    }
}

