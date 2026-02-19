package com.belden.topology.controller;

import com.belden.topology.service.ImpactAnalysisService;
import com.belden.topology.service.TopologyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/topology")
@RequiredArgsConstructor
public class TopologyController {

    private final TopologyService topologyService;
    private final ImpactAnalysisService impactService;

    // Builds the 3-cable scenario
    @PostMapping("/init")
    public String init() {
        topologyService.initializeFactoryFloor();
        return "SUCCESS: 3-Cable Factory Floor topology initialized in Neo4j.";
    }

    // Wipes the graph completely
    @DeleteMapping("/reset")
    public String reset() {
        topologyService.wipeDatabase();
        return "SUCCESS: Neo4j Graph wiped clean.";
    }

    // Existing simulate endpoint
    @GetMapping("/simulate/{cableId}")
    public String simulate(@PathVariable Long cableId) {
        String result = impactService.simulateFailure(cableId);
        return "If Cable " + cableId + " fails → Impacted: " + result;
    }
}