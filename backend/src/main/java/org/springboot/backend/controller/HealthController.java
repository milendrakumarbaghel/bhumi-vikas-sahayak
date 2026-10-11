package org.springboot.backend.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springboot.backend.integration.ai.GroundedAiClient;
import org.springboot.backend.repository.BuildingRuleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    private final BuildingRuleRepository rules;
    private final GroundedAiClient ai;

    public HealthController(BuildingRuleRepository rules, GroundedAiClient ai) {
        this.rules = rules;
        this.ai = ai;
    }

    @GetMapping("/live")
    public Map<String, String> live() {
        return Map.of("status", "UP", "application", "backend");
    }

    @GetMapping("/ready")
    public ResponseEntity<Map<String, Object>> ready() {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("application", "UP");
        try {
            long ruleCount = rules.count();
            state.put("database", "UP");
            state.put("ruleCount", ruleCount);
            state.put("aiService", ai.health());
            state.put("status", ruleCount > 0 ? "UP" : "DOWN");
            return ResponseEntity.status(ruleCount > 0 ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE).body(state);
        } catch (RuntimeException exception) {
            state.put("database", "DOWN");
            state.put("aiService", ai.health());
            state.put("status", "DOWN");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(state);
        }
    }
}
