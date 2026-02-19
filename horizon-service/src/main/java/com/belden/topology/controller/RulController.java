package com.belden.topology.controller;

import com.belden.topology.service.RulService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rul")
public class RulController {

    private final RulService rulService;

    public RulController(RulService rulService) {
        this.rulService = rulService;
    }


    @GetMapping("/{cableId}")
    public String getRUL(@PathVariable Long cableId) {

        double rul = rulService.calculateRUL(cableId);

        if (rul < 0) {
            return "Not enough data to calculate RUL";
        }

        return "Remaining Useful Life: " + Math.round(rul) + " days";
    }
}
