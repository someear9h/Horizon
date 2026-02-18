package com.belden.topology.service;

import com.belden.topology.model.CableTelemetry;
import com.belden.topology.repository.CableTelemetryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RulService {

    private final CableTelemetryRepository repository;

    private static final double FAILURE_THRESHOLD = 40.0;

    // Health calculation (mathematical model)
    public double calculateHealth(double attenuation,
                                  double temp,
                                  double load,
                                  int ageYears) {

        return 100
                - (2.0 * attenuation)
                - (1.5 * temp)
                - (1.2 * load)
                - (3.0 * ageYears);
    }

    // RUL Calculation
    public double calculateRUL(Long cableId) {
        List<CableTelemetry> records = repository.findTop2ByCableIdOrderByTimestampDesc(cableId);

        if (records.size() < 2) return -1;

        CableTelemetry latest = records.get(0);
        CableTelemetry previous = records.get(1);

        // ALREADY FAILED CHECK: If it's already below threshold, RUL is 0
        if (latest.getHealth() <= FAILURE_THRESHOLD) return 0;

        // Use Seconds for real-time demo accuracy
        long secsBetween = Duration.between(previous.getTimestamp(), latest.getTimestamp()).toSeconds();
        if (secsBetween == 0) secsBetween = 1;

        // Health difference
        double healthDrop = previous.getHealth() - latest.getHealth();

        // If health is improving or stable, we can't predict a failure point
        if (healthDrop <= 0) return -1;

        double degradationPerSec = healthDrop / secsBetween;

        // RUL = (Current Health - Failure Point) / Degradation Rate
        double secondsRemaining = (latest.getHealth() - FAILURE_THRESHOLD) / degradationPerSec;

        // Convert back to minutes for the response
        return secondsRemaining / 60.0;
    }
}
