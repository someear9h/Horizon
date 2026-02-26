package com.belden.topology.scheduler;

import com.belden.topology.model.CableTelemetry;
import com.belden.topology.repository.CableTelemetryRepository;
import com.belden.topology.repository.TopologyRepository;
import com.belden.topology.service.AlertService;
import com.belden.topology.service.RulService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class HealthAuditScheduler {

    private final TopologyRepository topologyRepository;
    private final RulService rulService;
    private final AlertService alertService;
    private final CableTelemetryRepository telemetryRepository;

    private int virtualDaysPassed = 0;
    private final int SIMULATION_STEP_DAYS = 5; // Jumps 5 days every tick

    @Scheduled(fixedRate = 2000)
    public void performGlobalHealthAudit() {
        virtualDaysPassed += SIMULATION_STEP_DAYS;

        topologyRepository.findAll().forEach(cable -> {
            telemetryRepository.findTopByCableIdOrderByTimestampDesc(cable.getCableId())
                    .ifPresent(lastData -> {

                        if (lastData.getHealth() <= 0.0) return;

                        System.out.println(">>> [AUTO-AUDIT] Simulating Decay for Cable-" + cable.getCableId());

                        // ✅ LAYER 1 DEGRADATION LOGIC (Tuned for EXACT 250-Day Death)
                        // These specific increments cause EXACTLY a -2.0 Health drop per tick in the service.
                        // 100 Health / 2.0 Drop = 50 Ticks.
                        // 50 Ticks * 5 Days = 250 Virtual Days!

                        double newTemp = lastData.getTemperature() + 1.0;     // Causes -0.15 Health
                        double newAttn = lastData.getAttenuation() + 0.1;     // Causes -0.25 Health
                        double newSnr = Math.max(0, lastData.getSnr() - 1.0); // Causes -0.40 Health
                        double newMse = lastData.getMse() + 0.06;             // Causes -1.20 Health

                        double currentLoad = lastData.getLoad();

                        // 3. CALCULATE ENRICHED HEALTH
                        double currentHealth = rulService.calculateHealth(
                                newAttn, newTemp, currentLoad, newSnr, newMse, 2
                        );

                        // 4. CALCULATE PREDICTIVE RUL
                        double rulDays = rulService.calculateRulDays(
                                currentHealth, lastData.getHealth(), SIMULATION_STEP_DAYS
                        );

                        // 5. ENRICH AND SAVE
                        CableTelemetry newRecord = CableTelemetry.builder()
                                .cableId(cable.getCableId())
                                .temperature(newTemp)
                                .attenuation(newAttn)
                                .snr(newSnr)
                                .mse(newMse)
                                .load(currentLoad)
                                .health(currentHealth)
                                .rulInDays(rulDays)
                                .timestamp(LocalDateTime.now())
                                .lastSeen(LocalDateTime.now())
                                .build();

                        telemetryRepository.save(newRecord);

                        System.out.println("STATUS: Cable-" + cable.getCableId() +
                                " | Health: " + Math.round(currentHealth) + "%" +
                                " | Day: " + virtualDaysPassed +
                                " | RUL: " + Math.round(rulDays) + " Days");

                        alertService.checkAndAlert(cable.getCableId(), currentHealth, rulDays);
                    });
        });
    }
}