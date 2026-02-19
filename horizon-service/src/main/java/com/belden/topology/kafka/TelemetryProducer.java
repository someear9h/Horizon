package com.belden.topology.kafka;

import com.belden.topology.model.CableTelemetry;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TelemetryProducer {

    private final KafkaTemplate<String, CableTelemetry> kafkaTemplate;
    private static final String TOPIC = "cable-telemetry";

    public void sendTelemetry(CableTelemetry telemetry) {
        // We use the cableId as the key so all messages for one cable stay in order
        kafkaTemplate.send(TOPIC, String.valueOf(telemetry.getCableId()), telemetry);
        System.out.println("Sent telemetry for Cable ID: " + telemetry.getCableId());
    }
}