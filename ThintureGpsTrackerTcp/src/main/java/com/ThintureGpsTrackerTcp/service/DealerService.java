package com.ThintureGpsTrackerTcp.service;

import com.ThintureGpsTrackerTcp.model.Dealer;
import com.ThintureGpsTrackerTcp.model.SerialNumber;
import com.ThintureGpsTrackerTcp.repository.DealerRepository;
import com.ThintureGpsTrackerTcp.repository.SerialNumberRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

// import java.util.List;

import java.util.Optional;
@Service
public class DealerService {

    private static final Logger logger = LoggerFactory.getLogger(DealerService.class);

    @Autowired
    private DealerRepository dealerRepository;
    @Autowired
    private SerialNumberRepository serialNumberRepository;
    
    
    
    
    //below login code start
    
    public boolean authenticateDealer(String username, String password, String usertype) {
        logger.info("[SERVICE] Authenticating user with username: {}, usertype: {}", username, usertype);

        if (!"DEALER".equalsIgnoreCase(usertype)) {
            logger.warn("[SERVICE] Usertype mismatch for username: {}. Expected: DEALER, Provided: {}", username, usertype);
            return false; // Reject login for non-DEALER user types
        }

        Optional<Dealer> dealerOptional = dealerRepository.findByEmail(username);
        if (dealerOptional.isPresent()) {
            Dealer dealer = dealerOptional.get();

            if (password.equals(dealer.getPassword())) {
                logger.info("[SERVICE] Authentication successful for DEALER with username: {}", username);
                return true;
            } else {
                logger.warn("[SERVICE] Password mismatch for username: {}", username);
                return false;
            }
        } else {
            logger.warn("[SERVICE] Dealer not found with username: {}", username);
            return false;
        }
      //login end code above
    }
    
    
    public List<Dealer> getDealers(String name) {
        logger.info("Fetching dealers containing name: {}", name);

        List<Dealer> dealers = dealerRepository.findByDealerNameContainingIgnoreCase(name);

        if (dealers.isEmpty()) {
            logger.warn("No dealers found for name: {}", name);
        } else {
            logger.info("Found {} dealers for name: {}", dealers.size(), name);
        }

        return dealers;
    }

    public void addSingleSerialNumber(String dealerName, String serialNumber) {
        logger.info("Adding single serial number '{}' to dealer '{}'", serialNumber, dealerName);

        Dealer dealer = dealerRepository.findByDealerName(dealerName)
                .orElseThrow(() -> {
                    logger.error("Dealer not found with name: {}", dealerName);
                    return new RuntimeException("Dealer not found");
                });

        SerialNumber serial = new SerialNumber();
        serial.setSerialNumber(serialNumber);
        serial.setDealer(dealer);

        serialNumberRepository.save(serial);

        logger.info("Successfully added serial number '{}' to dealer '{}'", serialNumber, dealerName);
    }

    public void addSerialNumbersInRange(String dealerName, int start, int end, Integer removedNumber) {
        logger.info("Adding serial numbers in range {}-{} for dealer '{}'. Removed number: {}",
                start, end, dealerName, removedNumber);

        Dealer dealer = dealerRepository.findByDealerName(dealerName)
                .orElseThrow(() -> {
                    logger.error("Dealer not found with name: {}", dealerName);
                    return new RuntimeException("Dealer not found");
                });

        int addedCount = 0;

        for (int i = start; i <= end; i++) {
            if (removedNumber != null && i == removedNumber) {
                logger.debug("Skipping removed serial number: {}", i);
                continue;
            }

            SerialNumber serial = new SerialNumber();
            serial.setSerialNumber(String.valueOf(i));
            serial.setDealer(dealer);
            serialNumberRepository.save(serial);
            addedCount++;
        }

        logger.info("Successfully added {} serial numbers to dealer '{}'", addedCount, dealerName);
    }
}
