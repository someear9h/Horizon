package com.belden.topology.service;

import com.belden.topology.repository.CableTelemetryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RulService {

    private final CableTelemetryRepository repository;

    // Base lifespan of a Belden industrial cable in days (approx 5 years for demo scale)
    private static final double DEMO_LIFESPAN_DAYS = 250.0;

    /**
     * Smoothed Health Calculation for Realistic Demo Curve
     */
    public double calculateHealth(double attenuation, double temp, double load, double snr, double mse, int ageYears) {
        double health = 100.0;

        // 1. Thermal Penalty - Extremely gradual heat impact
        if (temp > 25) {
            health -= (temp - 25) * 0.15;
        }

        // 2. Attenuation Penalty - Slow signal loss
        health -= (attenuation * 2.5);

        // 3. SNR Penalty - Gentler slope
        if (snr < 30) {
            health -= (30 - snr) * 0.4;
        }

        // 4. MSE Penalty - Smooth penalty as distortion rises
        health -= (mse * 20.0);

        // 5. Load and Age Baseline
        health -= (load * 0.02);
        health -= (ageYears * 1.0);

        // Clamp between 0 and 100 to prevent weird UI bugs
        return Math.max(0, Math.min(100, health));
    }

    /**
     * Exponential Decay Model for RUL.
     * Replaces the unstable "rate of change" math with a deterministic curve.
     */
//    public double calculateRulDays(double currentHealth, double previousHealth, int daysPassedInStep) {
//        if (currentHealth <= 0) return 0.0;
//        if (currentHealth >= 100) return MAX_LIFESPAN_DAYS;
//
//        // THE MAGIC DEMO MATH:
//        // By raising the health ratio to the power of 2.5, we create an exponential cliff.
//        // Health 100% -> 1825 Days
//        // Health 80%  -> ~1044 Days
//        // Health 50%  -> ~322 Days (Accelerating failure)
//        // Health 20%  -> ~32 Days (Critical Alert zone)
//
//        double healthRatio = currentHealth / 100.0;
//        double projectedRul = MAX_LIFESPAN_DAYS * Math.pow(healthRatio, 2.5);
//
//        return Math.max(0, projectedRul);
//    }
    public double calculateRulDays(double currentHealth,
                                   double previousHealth,
                                   int daysPassedInStep) {

        if (currentHealth <= 0) return 0.0;

        double healthDrop = previousHealth - currentHealth;

        // If no degradation, assume full remaining lifespan
        if (healthDrop <= 0) {
            return DEMO_LIFESPAN_DAYS;
        }

        // Health drop per day
        double dropPerDay = healthDrop / daysPassedInStep;

        // Days remaining until health reaches 0
        double remainingDays = currentHealth / dropPerDay;

        return Math.max(0, remainingDays);
    }

    // Keep this for legacy calls if needed, or remove if unused
    public double calculateRUL(Long cableId) {
        return 0.0;
    }
}