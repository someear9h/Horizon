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
@CrossOrigin(origins = "*") // Allow React on localhost:5173 to access this endpoint
public class ReportController {

    private final CableTelemetryRepository telemetryRepository;
    private final GeminiService geminiService;
    private final SustainabilityService sustainabilityService;

    @GetMapping("/{cableId}")
    public Map<String, String> getAiPoweredReport(@PathVariable Long cableId) {

        // 1. Fetch entire history
        List<CableTelemetry> history = telemetryRepository.findByCableIdOrderByTimestampAsc(cableId);

        if (history.isEmpty()) {
            return Map.of("aiExecutiveSummary", "No data found for Cable ID: " + cableId);
        }

        CableTelemetry firstRecord = history.get(0);
        CableTelemetry lastRecord = history.get(history.size() - 1);
        int virtualDaysSurvived = history.size() * 6; // Based on your 6-day simulation jump

        double avgTemp = history.stream().mapToDouble(CableTelemetry::getTemperature).average().orElse(0.0);
        double avgAttn = history.stream().mapToDouble(CableTelemetry::getAttenuation).average().orElse(0.0);

        // 2. Calculate Exact Carbon Match
        // We pass the final health to get the exact exact Avoided Carbon from your dynamic profiles
        CarbonMetrics exactMetrics = sustainabilityService.calculateMetrics(cableId, lastRecord.getHealth());

        // Round to 2 decimal places so the AI matches the UI perfectly (e.g., 23.23)
        double exactAvoidedCarbon = Math.round(exactMetrics.getAvoidedCarbonKg() * 100.0) / 100.0;

        // 3. Build the Raw Report
        CableLifecycleReport rawReport = CableLifecycleReport.builder()
                .cableId(cableId)
                .totalVirtualDaysSurvived(virtualDaysSurvived)
                .startingHealth(firstRecord.getHealth())
                .finalHealth(lastRecord.getHealth())
                .averageOperatingTemp(avgTemp)
                .averageAttenuation(avgAttn)
                .avoidedCarbonKg(exactAvoidedCarbon) // 4. Replaced the hardcoded 0.04 multiplier!
                .historicalTimeline(history)
                .build();

        // 4. Generate the AI Summary via Gemini 2.5 Flash
        String aiSummary = geminiService.generateExecutiveSummary(rawReport);

        // 5. Return ONLY the AI Summary to the frontend to save bandwidth
        return Map.of("aiExecutiveSummary", aiSummary);
    }
}