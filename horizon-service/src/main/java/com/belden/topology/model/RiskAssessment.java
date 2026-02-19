package com.belden.topology.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RiskAssessment {
    private int overallRiskScore;       // 0 (Safe) to 100 (Critical)

    // Risk Factors
    private double topologyCriticality; // How many machines die if this fails?
    private double assetHealthRisk;     // Inverse of current cable health
    private double environmentalStress; // Heat/Load impact

    // The "Money" Line
    private String insuranceStatus;     // "PREMIUM DISCOUNT APPROVED" or "HIGH RISK SURCHARGE"
    private String redundancyGap;       // "CRITICAL: Single Point of Failure Detected"
}