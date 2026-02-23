package com.belden.topology.service;

import com.belden.topology.model.Alert;
import com.belden.topology.repository.AlertRepository;
import com.belden.topology.repository.TopologyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final TopologyRepository topologyRepository;
    private final AlertRepository alertRepository;

    @Transactional
    public void checkAndAlert(Long cableId, double currentHealth, double estimatedRulHours) {
        double WARNING_THRESHOLD = 50.0;
        double CRITICAL_THRESHOLD = 20.0;

        if (currentHealth < WARNING_THRESHOLD) {
            // 1. Fetch data with a fallback to ensure the alert ALWAYS fires
            String impactData = topologyRepository.findImpactDetails(cableId).orElse("Unknown Machine::General Floor");

            String[] parts = impactData.split("::");
            String machineName = parts.length > 0 ? parts[0] : "Unknown Machine";
            String lineName = parts.length > 1 ? parts[1] : "General Floor";

            String severity = (currentHealth <= CRITICAL_THRESHOLD) ? "CRITICAL" : "WARNING";
            String timeDisplay = (estimatedRulHours <= 0) ? "IMMEDIATE" : String.format("%.1f Hours", estimatedRulHours);

            String message = String.format("Asset #%d (%s) on %s health dropped to %d%%. RUL: %s.",
                    cableId, machineName, lineName, Math.round(currentHealth), timeDisplay);

            // 2. Optimized Anti-Spam Check
            boolean alreadyExists = alertRepository.findByIsReadFalseOrderByTimestampDesc().stream()
                    .anyMatch(a -> a.getCableId().equals(cableId) && a.getSeverity().equals(severity));

            if (!alreadyExists) {
                Alert alert = Alert.builder()
                        .cableId(cableId)
                        .severity(severity)
                        .message(message)
                        .timestamp(LocalDateTime.now())
                        .isRead(false)
                        .build();

                alertRepository.save(alert);
                System.err.println(">>> [DATABASE ALERT SAVED] For Cable: " + cableId);
            }
        }
    }
}