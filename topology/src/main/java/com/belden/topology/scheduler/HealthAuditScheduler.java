package com.belden.topology.scheduler;

import com.belden.topology.repository.CableTelemetryRepository;
import com.belden.topology.repository.TopologyRepository;
import com.belden.topology.service.AlertService;
import com.belden.topology.service.RulService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HealthAuditScheduler {

    private final TopologyRepository topologyRepository;
    private final RulService rulService;
    private final AlertService alertService;
    private final CableTelemetryRepository telemetryRepository;

    // For the demo: Track how many "virtual days" have passed since the last POST
    private int virtualDaysPassed = 0;

    @Scheduled(fixedRate = 5000)
    public void performGlobalHealthAudit() {
        virtualDaysPassed += 10; // Jump 10 days every 5 seconds to speed up the demo
        System.out.println("\n>>> [AUTO-AUDIT] Virtual Day: " + virtualDaysPassed);

        topologyRepository.findAll().forEach(cable -> {
            telemetryRepository.findTopByCableIdOrderByTimestampDesc(cable.getCableId())
                    .ifPresent(lastData -> {
                        // Aggressive degradation for demo
                        // Slower degradation for a smoother demo countdown
                        double simulatedTemp = lastData.getTemperature() + (virtualDaysPassed * 0.5); // reduced from 2.0
                        double simulatedAttn = lastData.getAttenuation() + (virtualDaysPassed * 0.05); // reduced from 0.5

                        double currentHealth = rulService.calculateHealth(
                                simulatedAttn,
                                simulatedTemp,
                                lastData.getLoad(),
                                virtualDaysPassed
                        );

                        double rul = rulService.calculateRUL(cable.getCableId());

                        // THIS LINE IS KEY: Always see the progress
                        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                        System.out.println("STATUS: Cable-" + cable.getCableId() + " | Health: " + Math.round(currentHealth));
                        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

                        // Trigger the alert
                        alertService.checkAndAlert(cable.getCableId(), currentHealth, rul);
                    });
        });
    }
}