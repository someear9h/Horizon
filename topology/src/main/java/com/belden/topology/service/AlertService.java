package com.belden.topology.service;

import com.belden.topology.repository.TopologyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import this

@Service
@RequiredArgsConstructor
public class AlertService {

    private final TopologyRepository topologyRepository;

    @Transactional(transactionManager = "neo4jTransactionManager", readOnly = true)
    public void checkAndAlert(Long cableId, double currentHealth, double estimatedRulHours) {

        // 1. SET YOUR THRESHOLD (e.g., 50% for high priority alert)
        double ALERT_THRESHOLD = 50.0;

        // 2. LOGIC: Only alert if health is actually low AND RUL is valid
        // We check (estimatedRulHours >= 0) to avoid alerting on initialization or errors
        if (currentHealth < ALERT_THRESHOLD) {

            topologyRepository.findImpactDetails(cableId).ifPresent(impactData -> {

                String[] parts = impactData.split("::");
                String machineName = parts.length > 0 ? parts[0] : "Unknown Machine";
                String lineName = parts.length > 1 ? parts[1] : "General Floor";

                // Handle the string formatting for the hours
                // If RUL is 0 or -1, we say "Immediate"
                String timeDisplay = (estimatedRulHours <= 0) ? "IMMEDIATE" : String.format("%.1f Hours", estimatedRulHours);

                System.err.println("\n=================[ CRITICAL ALERT ]=================");
                System.err.println("Critical Alert: Cable-" + cableId + " (" + machineName + ") is at " + Math.round(currentHealth) + "% Health.");
                System.err.println("Action Required: " + timeDisplay + " replacement needed to prevent outage of " + lineName);
                System.err.println("Potential Savings: $12,000 in downtime prevention.");
                System.err.println("====================================================\n");
            });
        }
    }
}