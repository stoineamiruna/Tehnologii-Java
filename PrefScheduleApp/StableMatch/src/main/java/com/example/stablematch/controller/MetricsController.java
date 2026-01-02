package com.example.stablematch.controller;

import com.example.stablematch.service.MetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
@Tag(name = "Metrics", description = "Custom metrics endpoints")
public class MetricsController {

    private final MetricsService metricsService;

    @GetMapping("/matching")
    @Operation(summary = "Get matching algorithm metrics",
            description = "Returns counters and timers for matching algorithms")
    public ResponseEntity<Map<String, Object>> getMatchingMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        Map<String, Double> counters = new HashMap<>();
        counters.put("stable_match_invocations", metricsService.getStableMatchCount());
        counters.put("random_match_invocations", metricsService.getRandomMatchCount());

        Map<String, Double> timers = new HashMap<>();
        timers.put("stable_match_mean_time_ms", metricsService.getStableMatchMeanTime());
        timers.put("stable_match_max_time_ms", metricsService.getStableMatchMaxTime());

        metrics.put("counters", counters);
        metrics.put("timers", timers);

        return ResponseEntity.ok(metrics);
    }
}