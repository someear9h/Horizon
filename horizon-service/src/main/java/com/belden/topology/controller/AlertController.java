package com.belden.topology.controller;

import com.belden.topology.model.Alert;
import com.belden.topology.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/alerts")
@CrossOrigin(origins = "*")
public class AlertController {

    private final AlertRepository alertRepository;

    @GetMapping
    public List<Alert> getActiveAlerts() {
        return alertRepository.findByIsReadFalseOrderByTimestampDesc();
    }

    @PostMapping("/{id}/read")
    public void markAlertAsRead(@PathVariable Long id) {
        alertRepository.findById(id).ifPresent(alert -> {
            alert.setRead(true);
            alertRepository.save(alert);
        });
    }
}
