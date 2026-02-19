package com.belden.topology.service;

import com.belden.topology.model.RiskAssessment;
import com.belden.topology.repository.CableTelemetryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RiskScoringService {

    private final Neo4jClient neo4jClient;
    private final CableTelemetryRepository telemetryRepository;

    public RiskAssessment calculateFacilityRisk(Long cableId) {

        // 1. TOPOLOGY RISK (FIXED GRAPH QUERY)
        // We use -[*1..3]-> to traverse up to 3 hops downstream, regardless of relationship name.
        // This ensures we catch the Machines even if they are behind a Switch.
        Long downstreamNodes = neo4jClient.query(
                "MATCH (c:Cable) WHERE c.id = $id MATCH (c)-[*1..3]->(m:Machine) RETURN count(DISTINCT m)"
        ).bind(cableId).to("id").fetchAs(Long.class).one().orElse(0L);

        // Normalize: If > 3 machines depend on it, risk is MAX (100).
        double topologyRisk = Math.min(downstreamNodes * 33.3, 100.0);

        // 2. ASSET HEALTH RISK
        double currentHealth = telemetryRepository.findTopByCableIdOrderByTimestampDesc(cableId)
                .map(t -> t.getHealth())
                .orElse(100.0);

        double healthRisk = Math.max(0, 100.0 - currentHealth);

        // 3. ENVIRONMENTAL STRESS
        double currentTemp = telemetryRepository.findTopByCableIdOrderByTimestampDesc(cableId)
                .map(t -> t.getTemperature())
                .orElse(25.0);

        double envRisk = (currentTemp > 40) ? 80.0 : (currentTemp > 30 ? 40.0 : 10.0);

        // 4. WEIGHTED FORMULA
        // Topology (40%) + Health (40%) + Env (20%)
        double totalScore = (topologyRisk * 0.40) + (healthRisk * 0.40) + (envRisk * 0.20);
        int finalScore = (int) Math.round(totalScore);

        // 5. INSURANCE LOGIC
        String insuranceMsg;
        if (finalScore < 30) insuranceMsg = "✅ PREMIUM DISCOUNT APPLIED (-15%)";
        else if (finalScore < 60) insuranceMsg = "⚠️ STANDARD RATE (Action Advised)";
        else insuranceMsg = "🚫 DISCOUNT REVOKED (High Risk)";

        String redundancyMsg = (downstreamNodes > 0)
                ? "CRITICAL: Single Point of Failure (" + downstreamNodes + " Assets)"
                : "Topology Stable";

        return RiskAssessment.builder()
                .overallRiskScore(finalScore)
                .topologyCriticality(topologyRisk)
                .assetHealthRisk(healthRisk)
                .environmentalStress(envRisk)
                .insuranceStatus(insuranceMsg)
                .redundancyGap(redundancyMsg)
                .build();
    }
}