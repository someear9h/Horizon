package com.belden.topology.model;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class CableLifecycleReport {
    private Long cableId;
    private int totalVirtualDaysSurvived;
    private double startingHealth;
    private double finalHealth;
    private double averageOperatingTemp;
    private double averageAttenuation;
    private String primaryFailureCause;
    private double avoidedCarbonKg;
    private List<CableTelemetry> historicalTimeline;
}