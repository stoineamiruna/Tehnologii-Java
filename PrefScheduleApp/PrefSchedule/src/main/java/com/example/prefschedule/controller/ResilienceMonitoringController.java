package com.example.prefschedule.controller;

import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
@RestController
@RequestMapping("/api/resilience/monitor")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Resilience Monitoring", description = "Monitor resilience patterns state")
public class ResilienceMonitoringController {

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RateLimiterRegistry rateLimiterRegistry;
    private final BulkheadRegistry bulkheadRegistry;

    @GetMapping("/circuit-breaker/{name}")
    @Operation(summary = "Get circuit breaker state")
    public ResponseEntity<Map<String, Object>> getCircuitBreakerState(@PathVariable String name) {
        var circuitBreaker = circuitBreakerRegistry.circuitBreaker(name);
        var metrics = circuitBreaker.getMetrics();

        Map<String, Object> state = new HashMap<>();
        state.put("name", name);
        state.put("state", circuitBreaker.getState().toString());
        state.put("failureRate", metrics.getFailureRate());
        state.put("numberOfSuccessfulCalls", metrics.getNumberOfSuccessfulCalls());
        state.put("numberOfFailedCalls", metrics.getNumberOfFailedCalls());
        state.put("numberOfNotPermittedCalls", metrics.getNumberOfNotPermittedCalls());
        state.put("numberOfBufferedCalls", metrics.getNumberOfBufferedCalls());
        state.put("numberOfSlowCalls", metrics.getNumberOfSlowCalls());
        state.put("slowCallRate", metrics.getSlowCallRate());

        return ResponseEntity.ok(state);
    }

    @GetMapping("/rate-limiter/{name}")
    @Operation(summary = "Get rate limiter state")
    public ResponseEntity<Map<String, Object>> getRateLimiterState(@PathVariable String name) {
        var rateLimiter = rateLimiterRegistry.rateLimiter(name);
        var metrics = rateLimiter.getMetrics();

        Map<String, Object> state = new HashMap<>();
        state.put("name", name);
        state.put("availablePermissions", metrics.getAvailablePermissions());
        state.put("numberOfWaitingThreads", metrics.getNumberOfWaitingThreads());

        return ResponseEntity.ok(state);
    }

    @GetMapping("/bulkhead/{name}")
    @Operation(summary = "Get bulkhead state")
    public ResponseEntity<Map<String, Object>> getBulkheadState(@PathVariable String name) {
        var bulkhead = bulkheadRegistry.bulkhead(name);
        var metrics = bulkhead.getMetrics();

        Map<String, Object> state = new HashMap<>();
        state.put("name", name);
        state.put("availableConcurrentCalls", metrics.getAvailableConcurrentCalls());
        state.put("maxAllowedConcurrentCalls", metrics.getMaxAllowedConcurrentCalls());

        return ResponseEntity.ok(state);
    }

    @GetMapping("/all")
    @Operation(summary = "Get all resilience patterns state")
    public ResponseEntity<Map<String, Object>> getAllStates() {
        Map<String, Object> allStates = new HashMap<>();

        circuitBreakerRegistry.getAllCircuitBreakers().forEach(cb -> {
            var metrics = cb.getMetrics();
            Map<String, Object> cbState = new HashMap<>();
            cbState.put("state", cb.getState().toString());
            cbState.put("failureRate", metrics.getFailureRate());
            cbState.put("successfulCalls", metrics.getNumberOfSuccessfulCalls());
            cbState.put("failedCalls", metrics.getNumberOfFailedCalls());
            allStates.put("circuitBreaker_" + cb.getName(), cbState);
        });

        rateLimiterRegistry.getAllRateLimiters().forEach(rl -> {
            var metrics = rl.getMetrics();
            Map<String, Object> rlState = new HashMap<>();
            rlState.put("availablePermissions", metrics.getAvailablePermissions());
            rlState.put("waitingThreads", metrics.getNumberOfWaitingThreads());
            allStates.put("rateLimiter_" + rl.getName(), rlState);
        });

        bulkheadRegistry.getAllBulkheads().forEach(bh -> {
            var metrics = bh.getMetrics();
            Map<String, Object> bhState = new HashMap<>();
            bhState.put("availableCalls", metrics.getAvailableConcurrentCalls());
            bhState.put("maxCalls", metrics.getMaxAllowedConcurrentCalls());
            allStates.put("bulkhead_" + bh.getName(), bhState);
        });

        return ResponseEntity.ok(allStates);
    }

    @PostMapping("/circuit-breaker/{name}/transition-to-closed")
    @Operation(summary = "Force circuit breaker to CLOSED state")
    public ResponseEntity<String> transitionToClosed(@PathVariable String name) {
        var circuitBreaker = circuitBreakerRegistry.circuitBreaker(name);
        circuitBreaker.transitionToClosedState();
        log.info("Circuit breaker {} transitioned to CLOSED", name);
        return ResponseEntity.ok("Circuit breaker transitioned to CLOSED");
    }

    @PostMapping("/circuit-breaker/{name}/transition-to-open")
    @Operation(summary = "Force circuit breaker to OPEN state")
    public ResponseEntity<String> transitionToOpen(@PathVariable String name) {
        var circuitBreaker = circuitBreakerRegistry.circuitBreaker(name);
        circuitBreaker.transitionToOpenState();
        log.warn("Circuit breaker {} transitioned to OPEN", name);
        return ResponseEntity.ok("Circuit breaker transitioned to OPEN");
    }

    @PostMapping("/reset-all")
    @Operation(summary = "Reset all resilience patterns")
    public ResponseEntity<String> resetAll() {
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(cb -> {
            cb.reset();
            log.info("Reset circuit breaker: {}", cb.getName());
        });

        log.info("All resilience patterns reset");
        return ResponseEntity.ok("All resilience patterns reset");
    }
}