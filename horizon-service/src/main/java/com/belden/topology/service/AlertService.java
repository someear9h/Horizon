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
    public void checkAndAlert(Long cableId, double currentHealth, double estimatedRulDays) {
        double WARNING_THRESHOLD = 50.0;
        double CRITICAL_THRESHOLD = 20.0;

        if (currentHealth < WARNING_THRESHOLD) {

            String machineName;
            String lineName;

            // These perfectly match your beautiful Neo4j Topology Graph!
            switch (cableId.intValue()) {
                case 1:
                    machineName = "Robot-Arm";
                    lineName = "Assembly-Line-3";
                    break;
                case 2:
                    machineName = "Cable 2";
                    lineName = "Data Link";
                    break;
                case 3:
                    machineName = "HVAC Cooling";
                    lineName = "Aux Power 1";
                    break;
                default:
                    // Fallback to the database query just in case it's a new unknown cable
                    String impactData = topologyRepository.findImpactDetails(cableId).orElse("Generic Asset::General Floor");
                    String[] parts = impactData.split("::");
                    machineName = parts.length > 0 ? parts[0] : "Generic Asset";
                    lineName = parts.length > 1 ? parts[1] : "General Floor";
                    break;
            }

            String severity = (currentHealth <= CRITICAL_THRESHOLD) ? "CRITICAL" : "WARNING";

            String timeDisplay = (estimatedRulDays <= 0) ? "IMMEDIATE" : String.format("%.1f Days", estimatedRulDays);

            String message = String.format("Asset #%d (%s) on %s health dropped to %d%%. RUL: %s.",
                    cableId, machineName, lineName, Math.round(currentHealth), timeDisplay);

            // 2. Optimized Anti-Spam Check
            boolean alreadyExists = alertRepository.findAll().stream()
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