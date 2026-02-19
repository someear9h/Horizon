package com.belden.topology.service;

import com.belden.topology.repository.CableTelemetryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RulService {

    private final CableTelemetryRepository repository;

    // Health calculation (Standard Belden Model)
    public double calculateHealth(double attenuation, double temp, double load, int ageYears) {
        double health = 100.0;
        if (temp > 25) health -= (temp - 25) * 0.8;
        health -= (attenuation * 10.0);
        health -= (load * 0.1);
        health -= (ageYears * 2.0);
        return Math.max(0, health);
    }

    /**
     * Calculates RUL based on the exact rate of decay in the current simulation step.
     * @param currentHealth The health we just calculated
     * @param previousHealth The health 2 seconds ago
     * @param daysPassedInStep How many "virtual days" passed in this step (we use 5)
     * @return Estimated days until Health hits 0
     */
    public double calculateRulDays(double currentHealth, double previousHealth, int daysPassedInStep) {

        if (currentHealth <= 0) return 0.0;

        double healthDrop = previousHealth - currentHealth;

        // If health isn't dropping, life is infinite
        if (healthDrop <= 0) return 999.0;

        // Rate: How much health do we lose per virtual day?
        double dropPerDay = healthDrop / (double) daysPassedInStep;

        // Projection: How many days until we hit 0?
        return currentHealth / dropPerDay;
    }

    // Keep this for legacy calls if needed, or remove if unused
    public double calculateRUL(Long cableId) {
        return 0.0; // Placeholder as we moved logic to the method above
    }
}