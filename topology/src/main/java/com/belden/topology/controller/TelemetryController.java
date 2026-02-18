package com.belden.topology.controller;

import com.belden.topology.kafka.TelemetryProducer;
import com.belden.topology.model.CableTelemetry;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/telemetry")
public class TelemetryController {

    private final TelemetryProducer producer;

    public TelemetryController(TelemetryProducer producer) {
        this.producer = producer;
    }

    @PostMapping
    public String sendTelemetry(@RequestBody CableTelemetry telemetry) {
        System.out.println("Controller hit");
        producer.sendTelemetry(telemetry);
        return "Telemetry sent to Kafka successfully";
    }
}
