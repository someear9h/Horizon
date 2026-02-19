package com.belden.topology.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CarbonMetrics {
    private double embeddedCarbonKg;      // CO2 emitted to make this cable
    private double operationalCarbonKg;   // CO2 from energy loss (heat)
    private double avoidedCarbonKg;       // CO2 saved by not replacing early
    private String sustainabilityRating;  // A, B, C, D
}