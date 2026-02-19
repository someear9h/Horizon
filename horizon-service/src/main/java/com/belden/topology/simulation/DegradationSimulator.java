package com.belden.topology.simulation;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DegradationSimulator {

    private static final String API_URL = "http://localhost:8081/api/telemetry";
    private static final RestTemplate restTemplate = new RestTemplate();

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Starting Belden Cable Degradation Simulation...");

        // --- STEP 1: HEALTHY STATE ---
        // Expected Health: ~85
        System.out.println("Step 1: Sending Healthy Baseline...");
        sendTelemetry(1L, 30.0, 0.5, 20.0);

        System.out.println("Waiting 5 seconds to establish baseline...");
        TimeUnit.SECONDS.sleep(5);

        // --- STEP 2: WARNING STATE ---
        // Temp 55 (was 60) -> slightly less penalty
        // Attn 1.2 (was 1.5) -> slightly less penalty
        // Expected Health: ~60-65 (Safe buffer above 40)
        System.out.println("Step 2: Sending Warning Signs...");
        sendTelemetry(1L, 55.0, 1.2, 40.0);

        // This will now calculate as if ~5 days passed
        // Result should be approx 48-72 Hours remaining

        System.out.println("Waiting 5 seconds to demonstrate rapid failure...");
        TimeUnit.SECONDS.sleep(5);

        // --- STEP 3: FAILURE STATE ---
        // Expected Health: 0 (Dead)
        System.out.println("Step 3: Sending Critical Failure...");
        sendTelemetry(1L, 90.0, 5.0, 90.0);

        System.out.println("Simulation Complete.");
    }

    private static void sendTelemetry(Long cableId, double temp, double attenuation, double load) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> map = new HashMap<>();
        map.put("cableId", cableId);
        map.put("temperature", temp);
        map.put("attenuation", attenuation);
        map.put("load", load);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(map, headers);
        try {
            restTemplate.postForObject(API_URL, request, String.class);
            System.out.println(" -> Telemetry Sent: " + map);
        } catch (Exception e) {
            System.err.println("Failed to send telemetry: " + e.getMessage());
        }
    }
}