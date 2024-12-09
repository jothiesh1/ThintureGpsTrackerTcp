package com.ThintureGpsTrackerTcp.service;

import com.ThintureGpsTrackerTcp.model.Dealer;
import com.ThintureGpsTrackerTcp.repository.DealerRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

// import java.util.List;

import java.util.Optional;
@Service
public class DealerService {

    private static final Logger logger = LoggerFactory.getLogger(DealerService.class);

    @Autowired
    private DealerRepository dealerRepository;

    public boolean authenticateDealer(String username, String password) {
        logger.info("[SERVICE] Authenticating dealer with username: {}", username);

        Optional<Dealer> dealerOptional = dealerRepository.findByEmail(username);
        if (dealerOptional.isPresent()) {
            Dealer dealer = dealerOptional.get();

            // Plain-text password comparison
            if (password.equals(dealer.getPassword())) {
                logger.info("[SERVICE] Authentication successful for username: {}", username);
                return true;
            } else {
                logger.warn("[SERVICE] Password mismatch for username: {}", username);
                return false;
            }
        } else {
            logger.warn("[SERVICE] Dealer not found with username: {}", username);
            return false;
        }
    }
}
