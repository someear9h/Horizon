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
    private final int SIMULATION_STEP_DAYS = 5;

    @Scheduled(fixedRate = 2000)
    public void performGlobalHealthAudit() {
        virtualDaysPassed += SIMULATION_STEP_DAYS;

        topologyRepository.findAll().forEach(cable -> {
            telemetryRepository.findTopByCableIdOrderByTimestampDesc(cable.getCableId())
                    .ifPresent(lastData -> {

                        if (lastData.getHealth() <= 0.0) return;

                        System.out.println(">>> [AUTO-AUDIT] Simulating Decay for Cable-" + cable.getCableId());

                        // TUNED FOR EXACTLY 300 DAYS LINEAR DECAY
                        // By dropping SNR by 0.5 instead of 1.0, SNR will not hit zero before the cable dies.
                        // This prevents the decay line from bending.
                        double newTemp = lastData.getTemperature() + 0.5;     // Causes -0.075 Health
                        double newAttn = lastData.getAttenuation() + 0.1;     // Causes -0.250 Health
                        double newSnr = Math.max(0, lastData.getSnr() - 0.5); // Causes -0.200 Health
                        double newMse = lastData.getMse() + 0.0545;           // Causes -1.090 Health

                        double currentLoad = lastData.getLoad();

                        double currentHealth = rulService.calculateHealth(
                                newAttn, newTemp, currentLoad, newSnr, newMse, 2
                        );

                        double rulDays = rulService.calculateRulDays(
                                currentHealth, lastData.getHealth(), SIMULATION_STEP_DAYS
                        );

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