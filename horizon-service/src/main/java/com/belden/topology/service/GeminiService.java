package com.belden.topology.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeminiService {

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper; // Spring auto-injects this

    public String generateExecutiveSummary(Object reportData) {
        try {
            // 1. Minify the JSON to save tokens and speed up the API call
            String minifiedJson = objectMapper.writeValueAsString(reportData);

            // 2. The "Winner Level" System Prompt
            String prompt = "You are an AI Industrial Reliability Expert for Belden Horizon. " +
                    "Analyze the following JSON report of a failed Hirschmann network cable. " +
                    "Write a professional, crisp, 3-sentence executive summary for a Plant Manager. " +
                    "State the total virtual days survived, identify the root cause based on the final temperature/attenuation metrics, " +
                    "and highlight the avoided carbon footprint. Do not use markdown, keep it plain text. \n\n" +
                    "DATA: " + minifiedJson;

            // 3. Build the Gemini Request Payload
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", List.of(
                    Map.of("parts", List.of(
                            Map.of("text", prompt)
                    ))
            ));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // 4. Call the API
            String fullUrl = geminiApiUrl + geminiApiKey;
            JsonNode response = restTemplate.postForObject(fullUrl, request, JsonNode.class);

            // 5. Extract the generated text from the JSON response
            if (response != null && response.has("candidates")) {
                return response.get("candidates").get(0)
                        .get("content").get("parts").get(0)
                        .get("text").asText();
            }

            return "Report generation successful, but no AI summary was returned.";

        } catch (Exception e) {
            System.err.println("AI Generation Failed: " + e.getMessage());
            return "AI Summary unavailable at this time due to network limits. Please review the raw metrics.";
        }
    }
}