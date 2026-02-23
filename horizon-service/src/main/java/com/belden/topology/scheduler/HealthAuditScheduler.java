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
    private final int SIMULATION_STEP_DAYS = 6; // We jump 5 days every tick

    @Scheduled(fixedRate = 1000)
    public void performGlobalHealthAudit() {
        virtualDaysPassed += SIMULATION_STEP_DAYS;

        topologyRepository.findAll().forEach(cable -> {
            telemetryRepository.findTopByCableIdOrderByTimestampDesc(cable.getCableId())
                    .ifPresent(lastData -> {

                        // 1. STOP if dead
                        if (lastData.getHealth() <= 0.0) return;

                        System.out.println(">>> [AUTO-AUDIT] Simulating Decay for Cable-" + cable.getCableId());

                        // 2. DEGRADE
//                        double newTemp = lastData.getTemperature() + 4.0;
//                        double newAttn = lastData.getAttenuation() + 0.6;

                        double newTemp = lastData.getTemperature() + 0.5; // Slow heat rise
                        double newAttn = lastData.getAttenuation() + 0.05; // Gradual signal loss
                        double currentLoad = lastData.getLoad();

                        double currentHealth = rulService.calculateHealth(newAttn, newTemp, currentLoad, 2);

                        // 3. CALCULATE PERFECT RUL
                        // We use the exact drop between 'lastData.getHealth()' and 'currentHealth'
                        double rulDays = rulService.calculateRulDays(
                                currentHealth,
                                lastData.getHealth(),
                                SIMULATION_STEP_DAYS
                        );

                        // 4. SAVE
                        CableTelemetry newRecord = new CableTelemetry();
                        newRecord.setCableId(cable.getCableId());
                        newRecord.setTemperature(newTemp);
                        newRecord.setAttenuation(newAttn);
                        newRecord.setLoad(currentLoad);
                        newRecord.setHealth(currentHealth);
                        newRecord.setRulInDays(rulDays); // Save the synced prediction
                        newRecord.setTimestamp(LocalDateTime.now());
                        newRecord.setLastSeen(LocalDateTime.now());

                        telemetryRepository.save(newRecord);

                        // 5. ALERT
                        System.out.println("STATUS: Cable-" + cable.getCableId() +
                                " | Day: " + virtualDaysPassed +
                                " | Health: " + Math.round(currentHealth) +
                                " | RUL: " + Math.round(rulDays) + " Days");

                        alertService.checkAndAlert(cable.getCableId(), currentHealth, rulDays * 24);
                    });
        });
    }
}