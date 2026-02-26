package com.belden.topology.model;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class CableLifecycleReport {
    private Long cableId;
    private double startingHealth;
    private double finalHealth;
    private double averageOperatingTemp;
    private double averageAttenuation;
    private String primaryFailureCause;
    private double avoidedCarbonKg;
    private List<CableTelemetry> historicalTimeline;
    private double finalSnr;
    private double finalMse;

    private Map<String, CableTelemetry> degradationMilestones;
}