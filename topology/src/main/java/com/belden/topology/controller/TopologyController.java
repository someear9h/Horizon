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

    @PostMapping("/init")
    public String init() {
        topologyService.createSampleTopology();
        return "Sample topology created.";
    }

    @GetMapping("/simulate/{cableId}")
    public String simulate(@PathVariable Long cableId) {
        String result = impactService.simulateFailure(cableId);
        return "If Cable fails → Impacted: " + result;
    }
}