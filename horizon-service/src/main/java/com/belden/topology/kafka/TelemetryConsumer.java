package com.belden.topology.kafka;

import com.belden.topology.model.CableTelemetry;
import com.belden.topology.repository.CableTelemetryRepository;
import com.belden.topology.service.AlertService;
import com.belden.topology.service.RulService;
import lombok.AllArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class TelemetryConsumer {

    private final RulService rulService;
    private final CableTelemetryRepository repository;
    private final AlertService alertService;

    @KafkaListener(topics = "cable-telemetry", groupId = "rul-group-2")
    public void consume(CableTelemetry telemetry) {

        System.out.println(">>> [KAFKA] Received Telemetry for Cable-" + telemetry.getCableId());

        // 1. Fetch the previous state to calculate the drop
        var lastRecord = repository.findTopByCableIdOrderByTimestampDesc(telemetry.getCableId());
        double prevHealth = lastRecord.map(CableTelemetry::getHealth).orElse(100.0);

        // 2. Demo Polish: If Postman didn't send SNR or MSE, set them to healthy defaults
        // so the math doesn't crash to 0 immediately.
        if (telemetry.getSnr() == 0.0) telemetry.setSnr(30.0);
        if (telemetry.getMse() == 0.0) telemetry.setMse(0.01);

        // 3. Calculate Enriched Health using your MVP math
        double currentHealth = rulService.calculateHealth(
                telemetry.getAttenuation(),
                telemetry.getTemperature(),
                telemetry.getLoad(),
                telemetry.getSnr(),
                telemetry.getMse(),
                2 // Default age
        );

        // 4. Calculate Correct RUL (Using the same 5-day step as the Scheduler)
        double rulDays = rulService.calculateRulDays(currentHealth, prevHealth, 5);

        // 5. Update the object and Save to DB
        telemetry.setHealth(currentHealth);
        telemetry.setRulInDays(rulDays);
        telemetry.setTimestamp(LocalDateTime.now());
        telemetry.setLastSeen(LocalDateTime.now());

        repository.save(telemetry);

        System.out.println("<<< [KAFKA SAVED] Health: " + Math.round(currentHealth) +
                "% | RUL: " + Math.round(rulDays) + " Days");
    }
}