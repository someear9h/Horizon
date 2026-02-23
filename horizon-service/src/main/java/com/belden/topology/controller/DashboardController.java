package com.belden.topology.controller;

import com.belden.topology.model.*;
import com.belden.topology.repository.CableTelemetryRepository;
import com.belden.topology.service.RecommendationEngine;
import com.belden.topology.service.SustainabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.web.bind.annotation.*;
import com.belden.topology.service.RiskScoringService;

import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final Neo4jClient neo4jClient;
    private final CableTelemetryRepository telemetryRepository;
    private final SustainabilityService sustainabilityService;
    private final RiskScoringService riskService;
    private final RecommendationEngine recommendationEngine;

    // 1. Fetch Topology for Vis.js
    @GetMapping("/graph")
    public Map<String, Object> getGraph() {
        // We extract the first label from the array to use as the group
        Collection<Map<String, Object>> nodesRaw = neo4jClient.query(
                "MATCH (n) " +
                        "RETURN DISTINCT toString(elementId(n)) as id, " +
                        "head(labels(n)) as group, " + // Head gets the first label as a string
                        "n.name as label"
        ).fetch().all();

        Collection<Map<String, Object>> edgesRaw = neo4jClient.query(
                "MATCH (n)-[r]->(m) " +
                        "RETURN DISTINCT toString(elementId(n)) as from, " +
                        "toString(elementId(m)) as to"
        ).fetch().all();

        return Map.of("nodes", nodesRaw, "edges", edgesRaw);
    }

    // 2. Fetch History for Chart.js
    @GetMapping("/history/{cableId}")
    public List<CableTelemetry> getHistory(@PathVariable Long cableId) {
        // Return last 20 records for the graph
        return telemetryRepository.findByCableIdOrderByTimestampDesc(cableId);
    }

    @GetMapping("/carbon/{cableId}")
    public CarbonMetrics getCarbonMetrics(@PathVariable Long cableId) {
        // Fetch latest health to calculate efficiency loss
        return telemetryRepository.findTopByCableIdOrderByTimestampDesc(cableId)
                .map(t -> sustainabilityService.calculateMetrics(cableId, t.getHealth()))
                .orElse(CarbonMetrics.builder().build()); // Return empty if no data
    }

    @GetMapping("/risk/{cableId}")
    public RiskAssessment getRiskProfile(@PathVariable Long cableId) {
        return riskService.calculateFacilityRisk(cableId);
    }

    @GetMapping("/recommendations/{cableId}")
    public List<Recommendation> getRecommendations(@PathVariable Long cableId) {
        return recommendationEngine.generateRecommendations(cableId);
    }

    @GetMapping("/report/{cableId}")
    public CableLifecycleReport getCableLifecycleReport(@PathVariable Long cableId) {

        // 1. Fetch entire history sorted from birth to death
        List<CableTelemetry> history = telemetryRepository.findByCableIdOrderByTimestampAsc(cableId);

        if (history.isEmpty()) {
            return CableLifecycleReport.builder().cableId(cableId).build();
        }

        // 2. Extract key lifecycle events
        CableTelemetry firstRecord = history.get(0);
        CableTelemetry lastRecord = history.get(history.size() - 1);

        // Calculate total days survived (Assuming your simulator jumps 6 days per tick)
        int virtualDaysSurvived = history.size() * 6;

        // 3. Calculate Environmental Averages
        double avgTemp = history.stream().mapToDouble(CableTelemetry::getTemperature).average().orElse(0.0);
        double avgAttn = history.stream().mapToDouble(CableTelemetry::getAttenuation).average().orElse(0.0);

        // 4. AI "Root Cause" Diagnosis Logic
        String cause = "Standard Wear & Tear";
        if (avgTemp > 65.0) {
            cause = "Thermal Degradation (High Heat Exposure)";
        } else if (avgAttn > 3.0) {
            cause = "Physical Stress / EMI Interference";
        }

        // 5. Build and return the executive report
        return CableLifecycleReport.builder()
                .cableId(cableId)
                .totalVirtualDaysSurvived(virtualDaysSurvived)
                .startingHealth(firstRecord.getHealth())
                .finalHealth(lastRecord.getHealth())
                .averageOperatingTemp(avgTemp)
                .averageAttenuation(avgAttn)
                .primaryFailureCause(cause)
                .avoidedCarbonKg(virtualDaysSurvived * 0.04) // Small ESG metric
                .historicalTimeline(history) // The raw data for your React chart
                .build();
    }
}