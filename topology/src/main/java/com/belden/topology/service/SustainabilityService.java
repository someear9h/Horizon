package com.belden.topology.service;

import com.belden.topology.model.CarbonMetrics;
import org.springframework.stereotype.Service;

@Service
public class SustainabilityService {

    // --- REALISTIC CONSTANTS (Source: Industrial ESG Standards) ---
    private static final double CO2_MFG_COPPER = 4.7; // kg CO2 per kg Copper
    private static final double CO2_MFG_PVC = 2.4;    // kg CO2 per kg Plastic insulation
    private static final double CO2_TRANSPORT = 0.5;  // kg CO2 (avg shipping)

    // Standard "Blind" Replacement Interval (Industry Average: 3 Years)
    private static final double INDUSTRY_STD_LIFE_YEARS = 3.0;

    public CarbonMetrics calculateMetrics(Long cableId, double currentHealth) {

        // 1. PHYSICAL SPECS (Mocked for Cable-1 "Heavy Duty Power")
        double copperWeightKg = 3.5;
        double pvcWeightKg = 1.2;

        // 2. EMBEDDED CARBON (Manufacturing + Transport)
        // Formula: (Mat1 * Factor) + (Mat2 * Factor) + Logistics
        double manufacturingImpact = (copperWeightKg * CO2_MFG_COPPER) + (pvcWeightKg * CO2_MFG_PVC);
        double totalEmbedded = manufacturingImpact + CO2_TRANSPORT;

        // 3. OPERATIONAL CARBON (Heat Loss)
        // A degraded cable runs hotter -> more resistance -> wasted energy (I^2R loss)
        // If Health is 80%, efficiency loss is small. If 20%, loss is high.
        double efficiencyLossFactor = (100 - currentHealth) * 0.05; // 0% to 5% extra waste
        double baseEnergyC02 = 1.2; // Baseline annual operational CO2
        double operationalImpact = baseEnergyC02 * (1 + efficiencyLossFactor);

        // 4. AVOIDED CARBON (The "Killer" Metric)
        // Logic: By using AI to extend life beyond the 3-year standard, we save a % of a new cable.
        // We simulate that this cable has lasted longer than the industry standard.
        double theoreticalLifeExtension = 1.5; // We assume AI gets us 50% more life
        double avoidedImpact = (totalEmbedded / INDUSTRY_STD_LIFE_YEARS) * theoreticalLifeExtension;

        // 5. RATING
        String rating = "A+";
        if (currentHealth < 50) rating = "B";
        if (currentHealth < 20) rating = "C";

        return CarbonMetrics.builder()
                .embeddedCarbonKg(totalEmbedded)
                .operationalCarbonKg(operationalImpact)
                .avoidedCarbonKg(avoidedImpact)
                .sustainabilityRating(rating)
                .build();
    }
}