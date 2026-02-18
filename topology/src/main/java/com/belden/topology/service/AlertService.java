package com.belden.topology.service;

import com.belden.topology.repository.TopologyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import this

@Service
@RequiredArgsConstructor
public class AlertService {

    private final TopologyRepository topologyRepository;

    // Fix: Explicitly use the neo4jTransactionManager we just created
    @Transactional(transactionManager = "neo4jTransactionManager", readOnly = true)
    public void checkAndAlert(Long cableId, double currentHealth, double estimatedRulMinutes) {

        if (currentHealth < 70.0 || (estimatedRulMinutes > 0 && estimatedRulMinutes < 2880)) {

            // This call will now succeed because it has a valid Neo4j transaction
            String impactData = topologyRepository.findImpactDetails(cableId)
                    .orElse("Unknown Machine::Unknown Line");

            String[] parts = impactData.split("::");
            String machineName = parts.length > 0 ? parts[0] : "Unknown Machine";
            String lineName = parts.length > 1 ? parts[1] : "General Floor";

            String timeRemaining = String.format("%.1f Hours", estimatedRulMinutes / 60.0);

            System.err.println("\n=================[ CRITICAL ALERT ]=================");
            System.err.println("Critical Alert: Cable-" + cableId + " (" + machineName + ") has " + timeRemaining + " of life remaining.");
            System.err.println("Replacing this now prevents an outage of " + lineName + ", saving $12,000 in potential downtime.");
            System.err.println("====================================================\n");
        }
    }
}