package com.belden.topology.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RulService {

    private static final double DEMO_LIFESPAN_DAYS = 300.0;

    public double calculateHealth(double attenuation, double temp, double load, double snr, double mse, int ageYears) {
        double health = 100.0;

        if (temp > 25) {
            double excessHeat = temp - 25;
            if (temp > 60) {
                //EXTREME HEAT: Cable insulation is melting.
                // Penalize much harder to crash RUL to < 50 days.
                health -= (excessHeat * 0.8);
            } else {
                // 🚀 NORMAL OPS: Keep the gentle decay you already tuned.
                health -= (excessHeat * 0.15);
            }
        }
        health -= (attenuation * 2.5);
        if (snr < 30) health -= (30 - snr) * 0.4;
        health -= (mse * 20.0);
        health -= (load * 0.02);
        health -= (ageYears * 1.0);

        return Math.max(0, Math.min(100, health));
    }

    public double calculateRulDays(double currentHealth, double previousHealth, int daysPassedInStep) {
        if (currentHealth <= 0) return 0.0;

        double healthDrop = previousHealth - currentHealth;

        if (healthDrop <= 0) {
            return DEMO_LIFESPAN_DAYS;
        }

        double dropPerDay = healthDrop / daysPassedInStep;
        double remainingDays = currentHealth / dropPerDay;

        return Math.max(0, remainingDays);
    }
}