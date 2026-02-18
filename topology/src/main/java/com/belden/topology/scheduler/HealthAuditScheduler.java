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

    // Runs every 2 seconds for a faster, smoother demo graph
    @Scheduled(fixedRate = 2000)
    public void performGlobalHealthAudit() {

        // We increment this to track simulation time globally
        virtualDaysPassed += 5;

        topologyRepository.findAll().forEach(cable -> {
            // Fetch the LATEST record (which might be the one we just saved 2 seconds ago)
            telemetryRepository.findTopByCableIdOrderByTimestampDesc(cable.getCableId())
                    .ifPresent(lastData -> {

                        // 1. STOP CONDITION: If health is already 0, stop adding rows.
                        // This prevents spamming the database with dead records.
                        if (lastData.getHealth() <= 0.0) {
                            return;
                        }

                        System.out.println(">>> [AUTO-AUDIT] Simulating Decay for Cable-" + cable.getCableId());

                        // 2. GRADUAL DEGRADATION LOGIC
                        // We take the LAST RECORD and make it slightly worse.
                        double newTemp = lastData.getTemperature() + 4.0;  // Heat rises gradually
                        double newAttn = lastData.getAttenuation() + 0.6;  // Signal loss increases
                        double currentLoad = lastData.getLoad();

                        // Calculate Health based on these new "worse" values
                        double currentHealth = rulService.calculateHealth(
                                newAttn,
                                newTemp,
                                currentLoad,
                                2
                        );

                        // 3. Save new state
                        CableTelemetry newRecord = new CableTelemetry();
                        newRecord.setCableId(cable.getCableId());
                        newRecord.setTemperature(newTemp);
                        newRecord.setAttenuation(newAttn);
                        newRecord.setLoad(currentLoad);
                        newRecord.setHealth(currentHealth);
                        newRecord.setTimestamp(LocalDateTime.now());
                        newRecord.setLastSeen(LocalDateTime.now());

                        telemetryRepository.save(newRecord);

                        // 4. Trigger Alert
                        double rul = rulService.calculateRUL(cable.getCableId());
                        System.out.println("STATUS: Cable-" + cable.getCableId() + " | Health: " + Math.round(currentHealth) + "%");
                        alertService.checkAndAlert(cable.getCableId(), currentHealth, rul);
                    });
        });
    }
}