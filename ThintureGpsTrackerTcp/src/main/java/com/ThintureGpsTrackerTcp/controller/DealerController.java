package com.ThintureGpsTrackerTcp.controller;
import com.ThintureGpsTrackerTcp.model.Dealer;
import com.ThintureGpsTrackerTcp.repository.DealerRepository;
import com.ThintureGpsTrackerTcp.service.DealerService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Optional;
//package com.ThintureGpsTrackerTcp.controller;



import java.util.Optional;


@RestController
@RequestMapping("/api/dealers")
public class DealerController {

    @Autowired
    private DealerRepository dealerRepository;

    @GetMapping("/all")
    public List<Dealer> getAllDealers() {
        return dealerRepository.findAll();
    }
}