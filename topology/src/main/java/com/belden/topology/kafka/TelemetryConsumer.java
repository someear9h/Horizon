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
        System.out.println("Kafka Message received: " + telemetry);

        // 1. Calculate the current Health Index
        double health = rulService.calculateHealth(
                telemetry.getAttenuation(),
                telemetry.getTemperature(),
                telemetry.getLoad(),
                2
        );

        // 2. Set the calculated fields
        telemetry.setHealth(health);
        telemetry.setTimestamp(LocalDateTime.now());

        // 3. PERSIST to the database so calculateRUL can find it later
        repository.save(telemetry);
        System.out.println("Saved telemetry with Health: " + health);

        double rulMinutes = rulService.calculateRUL(telemetry.getCableId());

        // check for critical impacts
        alertService.checkAndAlert(telemetry.getCableId(), health, rulMinutes);
    }
}
