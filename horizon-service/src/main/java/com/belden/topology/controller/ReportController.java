package com.belden.topology.controller;

import com.belden.topology.model.CableLifecycleReport;
import com.belden.topology.model.CableTelemetry;
import com.belden.topology.model.CarbonMetrics;
import com.belden.topology.repository.CableTelemetryRepository;
import com.belden.topology.service.GeminiService;
import com.belden.topology.service.SustainabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReportController {

    private final CableTelemetryRepository telemetryRepository;
    private final GeminiService geminiService;
    private final SustainabilityService sustainabilityService;

    @GetMapping("/{cableId}")
    public Map<String, String> getAiPoweredReport(@PathVariable Long cableId) {

        List<CableTelemetry> history = telemetryRepository.findByCableIdOrderByTimestampAsc(cableId);

        if (history.isEmpty()) {
            return Map.of("aiExecutiveSummary", "No data found for Cable ID: " + cableId);
        }

        CableTelemetry firstRecord = history.get(0);
        CableTelemetry lastRecord = history.get(history.size() - 1);



        // 1. Extract Final Layer 1 Metrics for AI Analysis
        double finalSnr = lastRecord.getSnr();
        double finalMse = lastRecord.getMse();
        double avgTemp = history.stream().mapToDouble(CableTelemetry::getTemperature).average().orElse(0.0);
        double avgAttn = history.stream().mapToDouble(CableTelemetry::getAttenuation).average().orElse(0.0);

        // 2. Calculate Exact Carbon Match
        CarbonMetrics exactMetrics = sustainabilityService.calculateMetrics(cableId, lastRecord.getHealth());
        double exactAvoidedCarbon = Math.round(exactMetrics.getAvoidedCarbonKg() * 100.0) / 100.0;

        // 3. Build the Raw Report with SNR and MSE
        // Ensure your CableLifecycleReport model has .finalSnr() and .finalMse() fields!
        CableLifecycleReport rawReport = CableLifecycleReport.builder()
                .cableId(cableId)
                .startingHealth(firstRecord.getHealth())
                .finalHealth(lastRecord.getHealth())
                .averageOperatingTemp(avgTemp)
                .averageAttenuation(avgAttn)
                .finalSnr(finalSnr)
                .finalMse(finalMse)
                .avoidedCarbonKg(exactAvoidedCarbon)
                .historicalTimeline(history)
                .build();

        // 4. Generate the AI Summary
        // The prompt we updated in GeminiService will now find these SNR/MSE values in the JSON
        String aiSummary = geminiService.generateExecutiveSummary(rawReport);

        return Map.of("aiExecutiveSummary", aiSummary);
    }
}