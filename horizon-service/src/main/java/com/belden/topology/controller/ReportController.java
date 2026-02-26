package com.belden.topology.controller;

import com.belden.topology.model.CableLifecycleReport;
import com.belden.topology.model.CableTelemetry;
import com.belden.topology.model.CarbonMetrics;
import com.belden.topology.repository.CableTelemetryRepository;
import com.belden.topology.service.GeminiService;
import com.belden.topology.service.SustainabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
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

        // Since the first record is Day 0, we subtract 1 from the size and multiply by the 5-day step.
        int finalDay = (history.size() - 1) * 5;

        Map<String, CableTelemetry> milestones = new LinkedHashMap<>();
        for (int i = 0; i < history.size(); i++) {
            int virtualDay = i * 5;

            // Grab Day 50, 100, 150, 200, 250
            if (virtualDay > 0 && virtualDay % 50 == 0) {
                milestones.put("Day " + virtualDay, history.get(i));
            }
        }

        milestones.put("Final Status (Day " + finalDay + ")", lastRecord);

        double finalSnr = lastRecord.getSnr();
        double finalMse = lastRecord.getMse();
        double avgTemp = history.stream().mapToDouble(CableTelemetry::getTemperature).average().orElse(0.0);
        double avgAttn = history.stream().mapToDouble(CableTelemetry::getAttenuation).average().orElse(0.0);

        CarbonMetrics exactMetrics = sustainabilityService.calculateMetrics(cableId, lastRecord.getHealth());
        double exactAvoidedCarbon = Math.round(exactMetrics.getAvoidedCarbonKg() * 100.0) / 100.0;

        CableLifecycleReport rawReport = CableLifecycleReport.builder()
                .cableId(cableId)
                .startingHealth(firstRecord.getHealth())
                .finalHealth(lastRecord.getHealth())
                .averageOperatingTemp(avgTemp)
                .averageAttenuation(avgAttn)
                .finalSnr(finalSnr)
                .finalMse(finalMse)
                .avoidedCarbonKg(exactAvoidedCarbon)
                .historicalTimeline(null)
                .degradationMilestones(milestones)
                .build();

        String aiSummary = geminiService.generateExecutiveSummary(rawReport);

        return Map.of("aiExecutiveSummary", aiSummary);
    }
}