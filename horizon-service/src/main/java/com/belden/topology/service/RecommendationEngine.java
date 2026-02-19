package com.belden.topology.service;

import com.belden.topology.model.CableTelemetry;
import com.belden.topology.model.Recommendation;
import com.belden.topology.model.RiskAssessment;
import com.belden.topology.repository.CableTelemetryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationEngine {

    private final CableTelemetryRepository telemetryRepository;
    private final RiskScoringService riskScoringService;

    public List<Recommendation> generateRecommendations(Long cableId) {
        List<Recommendation> recommendations = new ArrayList<>();

        CableTelemetry telemetry = telemetryRepository.findTopByCableIdOrderByTimestampDesc(cableId).orElse(null);
        if (telemetry == null) return recommendations;

        RiskAssessment risk = riskScoringService.calculateFacilityRisk(cableId);
        double rulDays = telemetry.getRulInDays();

        // ---------------------------------------------------------
        // RULE 1: MAINTENANCE (Driven by RUL Math)
        // ---------------------------------------------------------
        if (rulDays <= 60 && rulDays >= 0) {

            // If health is 0, confidence is 100%. Otherwise, calculate based on days.
            int confidence = (telemetry.getHealth() <= 0) ? 100 : (int) Math.min(99, 100 - (rulDays * 0.5));

            String title = (telemetry.getHealth() <= 0) ? "CRITICAL: Replace Cable Immediately" : "Replace Cable-" + cableId + " Imminently";
            String desc = (telemetry.getHealth() <= 0) ? "Asset has failed. Immediate replacement required to restore operations." : "Asset is approaching physical failure boundary within " + Math.round(rulDays) + " days.";

            recommendations.add(Recommendation.builder()
                    .category("MAINTENANCE")
                    .title(title)
                    .description(desc)
                    .businessImpact("Prevents $12,000 in unplanned downtime.")
                    .confidenceScore(confidence)
                    .actionLabel("Dispatch Technician Now")
                    .build());
        }

        // ---------------------------------------------------------
        // RULE 2: RESILIENCE UPSELL (Driven by Graph Centrality)
        // ---------------------------------------------------------
        // If Topology Criticality > 50, it means multiple machines rely on this single path.
        if (risk.getTopologyCriticality() > 50.0) {
            recommendations.add(Recommendation.builder()
                    .category("RESILIENCE")
                    .title("Eliminate Single Point of Failure")
                    .description("Graph analysis detects bottleneck. Add parallel path.")
                    .businessImpact("Install Belden Hirschmann™ Redundant Switch to drop Risk Score by 40 pts.")
                    .confidenceScore(92)
                    .actionLabel("View Hardware")
                    .build());
        }

        // ---------------------------------------------------------
        // RULE 3: SUSTAINABILITY UPSELL (Driven by Heat/Energy Loss)
        // ---------------------------------------------------------
        if (telemetry.getTemperature() > 30.0) {
            // Hotter cables waste more energy. Math calculates efficiency probability.
            int thermalConfidence = (int) Math.min(99, 60 + (telemetry.getTemperature() - 30) * 2);
            recommendations.add(Recommendation.builder()
                    .category("SUSTAINABILITY")
                    .title("Upgrade to Belden EcoLine™")
                    .description("Thermal inefficiency detected. High resistance is generating waste heat.")
                    .businessImpact("Gain 18% energy efficiency & improve ESG Rating to A+.")
                    .confidenceScore(thermalConfidence)
                    .actionLabel("Calculate ROI")
                    .build());
        }

        // Default recommendation if everything is perfect
        if (recommendations.isEmpty()) {
            recommendations.add(Recommendation.builder()
                    .category("OPTIMIZATION")
                    .title("Infrastructure Optimized")
                    .description("Current topology and asset health are operating within optimal parameters.")
                    .businessImpact("No action required.")
                    .confidenceScore(100)
                    .actionLabel("Dismiss")
                    .build());
        }

        return recommendations;
    }
}