package com.belden.topology.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Recommendation {
    private String category;        // MAINTENANCE, RESILIENCE, SUSTAINABILITY
    private String title;           // e.g., "Upgrade to Belden EcoLine™"
    private String description;     // The "Why"
    private String businessImpact;  // The ROI (e.g., "Saves $12,000")
    private int confidenceScore;    // ML Probability %
    private String actionLabel;     // e.g., "Order Now"
}