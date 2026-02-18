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
    public double calculateHealth(double attenuation, double temp, double load, int ageYears) {
        double health = 100.0;
        if (temp > 25) health -= (temp - 25) * 0.8;
        health -= (attenuation * 10.0);
        health -= (load * 0.1);
        health -= (ageYears * 2.0);
        return Math.max(0, health);
    }

    public double calculateRUL(Long cableId) {
        List<CableTelemetry> records = repository.findTop2ByCableIdOrderByTimestampDesc(cableId);

        if (records.size() < 2) return -1;

        CableTelemetry latest = records.get(0);
        CableTelemetry previous = records.get(1);

        // 1. Check if already failed
        if (latest.getHealth() <= FAILURE_THRESHOLD) return 0;

        // 2. Calculate Real Time elapsed
        long realSecondsBetween = Duration.between(previous.getTimestamp(), latest.getTimestamp()).toSeconds();
        if (realSecondsBetween == 0) realSecondsBetween = 1;

        // 3. DEMO MAGIC: Time Dilation
        // Treat 1 Real Second as 1 Virtual Day (86400 seconds)
        // This makes the degradation look realistic over a long period
        long virtualSecondsBetween = realSecondsBetween * 86400;

        double healthDrop = previous.getHealth() - latest.getHealth();

        // If health is stable, return -1 (No degradation)
        if (healthDrop <= 0) return -1;

        // 4. Calculate Rate based on VIRTUAL time
        double degradationPerVirtualSec = healthDrop / virtualSecondsBetween;

        // 5. RUL Calculation
        double virtualSecondsRemaining = (latest.getHealth() - FAILURE_THRESHOLD) / degradationPerVirtualSec;

        // Return Hours
        return virtualSecondsRemaining / 3600.0;
    }
}
