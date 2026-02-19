package com.belden.topology.controller;

import com.belden.topology.model.CableTelemetry;
import com.belden.topology.repository.CableTelemetryRepository;
import com.belden.topology.service.SustainabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.neo4j.core.Neo4jClient;
import com.belden.topology.model.CarbonMetrics;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final Neo4jClient neo4jClient;
    private final CableTelemetryRepository telemetryRepository;
    private final SustainabilityService sustainabilityService;

    // 1. Fetch Topology for Vis.js
    @GetMapping("/graph")
    public Map<String, Object> getGraph() {
        // Fetch Nodes: We explicitly cast IDs to String to be safe for the frontend
        Collection<Map<String, Object>> nodesRaw = neo4jClient.query(
                "MATCH (n) RETURN toString(elementId(n)) as id, labels(n) as group, n.name as label"
        ).fetch().all();

        // Fetch Edges: We match IDs to strings here too
        Collection<Map<String, Object>> edgesRaw = neo4jClient.query(
                "MATCH (n)-[r]->(m) RETURN toString(elementId(n)) as from, toString(elementId(m)) as to"
        ).fetch().all();

        return Map.of(
                "nodes", nodesRaw,
                "edges", edgesRaw
        );
    }

    // 2. Fetch History for Chart.js
    @GetMapping("/history/{cableId}")
    public List<CableTelemetry> getHistory(@PathVariable Long cableId) {
        // Return last 20 records for the graph
        return telemetryRepository.findTop20ByCableIdOrderByTimestampDesc(cableId);
    }

    @GetMapping("/carbon/{cableId}")
    public CarbonMetrics getCarbonMetrics(@PathVariable Long cableId) {
        // Fetch latest health to calculate efficiency loss
        return telemetryRepository.findTopByCableIdOrderByTimestampDesc(cableId)
                .map(t -> sustainabilityService.calculateMetrics(cableId, t.getHealth()))
                .orElse(CarbonMetrics.builder().build()); // Return empty if no data
    }
}