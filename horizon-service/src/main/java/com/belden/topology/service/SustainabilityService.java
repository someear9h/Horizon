package com.belden.topology.service;

import com.belden.topology.model.CarbonMetrics;
import org.springframework.stereotype.Service;

@Service
public class SustainabilityService {

    private static final double CO2_MFG_COPPER = 4.7;
    private static final double CO2_MFG_PVC = 2.4;
    private static final double CO2_TRANSPORT = 0.5;
    private static final double INDUSTRY_STD_LIFE_YEARS = 3.0;

    public CarbonMetrics calculateMetrics(Long cableId, double currentHealth) {

        double copperWeightKg = 0.0;
        double pvcWeightKg = 0.0;

        // DYNAMIC CABLE PROFILES
        if (cableId == 1) {
            // Cable 1: Heavy Duty Main Power (Massive Footprint)
            copperWeightKg = 8.5;
            pvcWeightKg = 2.5;
        } else if (cableId == 2) {
            // Cable 2: Data Link (Small Footprint)
            copperWeightKg = 0.8;
            pvcWeightKg = 1.5;
        } else if (cableId == 3) {
            // Cable 3: Aux Power (Medium Footprint)
            copperWeightKg = 3.5;
            pvcWeightKg = 1.2;
        }

        double manufacturingImpact = (copperWeightKg * CO2_MFG_COPPER) + (pvcWeightKg * CO2_MFG_PVC);
        double totalEmbedded = manufacturingImpact + CO2_TRANSPORT;

        double efficiencyLossFactor = (100 - currentHealth) * 0.15;
        double baseEnergyC02 = 1.2;
        double operationalImpact = baseEnergyC02 * (1 + efficiencyLossFactor);

        double theoreticalLifeExtension = 1.5;
        double avoidedImpact = (totalEmbedded / INDUSTRY_STD_LIFE_YEARS) * theoreticalLifeExtension;

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