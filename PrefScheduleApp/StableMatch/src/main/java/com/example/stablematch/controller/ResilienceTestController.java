package com.example.stablematch.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/test/resilience")
@Slf4j
@Tag(name = "Resilience Testing", description = "Endpoints for testing resilience patterns")
public class ResilienceTestController {

    private final AtomicBoolean simulateFailure = new AtomicBoolean(false);
    private final AtomicBoolean simulateSlowResponse = new AtomicBoolean(false);
    private final AtomicInteger requestCount = new AtomicInteger(0);
    private final AtomicInteger slowResponseDelay = new AtomicInteger(5000); // 5 seconds default
    private final Random random = new Random();

    @GetMapping("/status")
    @Operation(summary = "Get current test configuration")
    public ResponseEntity<TestStatus> getStatus() {
        return ResponseEntity.ok(TestStatus.builder()
                .failureEnabled(simulateFailure.get())
                .slowResponseEnabled(simulateSlowResponse.get())
                .slowResponseDelay(slowResponseDelay.get())
                .requestCount(requestCount.get())
                .build());
    }

    @PostMapping("/enable-failure")
    @Operation(summary = "Enable failure simulation",
            description = "All requests will fail with 500 error")
    public ResponseEntity<String> enableFailure() {
        simulateFailure.set(true);
        log.warn("Failure simulation ENABLED - all requests will fail");
        return ResponseEntity.ok("Failure simulation enabled");
    }

    @PostMapping("/disable-failure")
    @Operation(summary = "Disable failure simulation")
    public ResponseEntity<String> disableFailure() {
        simulateFailure.set(false);
        log.info("Failure simulation DISABLED");
        return ResponseEntity.ok("Failure simulation disabled");
    }

    @PostMapping("/enable-slow-response")
    @Operation(summary = "Enable slow response simulation",
            description = "All requests will take configured delay to respond")
    public ResponseEntity<String> enableSlowResponse(@RequestParam(defaultValue = "5000") int delayMs) {
        simulateSlowResponse.set(true);
        slowResponseDelay.set(delayMs);
        log.warn("Slow response simulation ENABLED - delay: {}ms", delayMs);
        return ResponseEntity.ok("Slow response enabled with " + delayMs + "ms delay");
    }

    @PostMapping("/disable-slow-response")
    @Operation(summary = "Disable slow response simulation")
    public ResponseEntity<String> disableSlowResponse() {
        simulateSlowResponse.set(false);
        log.info("Slow response simulation DISABLED");
        return ResponseEntity.ok("Slow response disabled");
    }

    @PostMapping("/random-failures")
    @Operation(summary = "Enable random failures",
            description = "50% of requests will fail randomly")
    public ResponseEntity<String> enableRandomFailures() {
        simulateFailure.set(true);
        log.warn("Random failure simulation ENABLED - 50% failure rate");
        return ResponseEntity.ok("Random failures enabled (50% rate)");
    }

    @PostMapping("/reset")
    @Operation(summary = "Reset all test configurations")
    public ResponseEntity<String> reset() {
        simulateFailure.set(false);
        simulateSlowResponse.set(false);
        requestCount.set(0);
        slowResponseDelay.set(5000);
        log.info("All test configurations RESET");
        return ResponseEntity.ok("All configurations reset");
    }

    @GetMapping("/test-endpoint")
    @Operation(summary = "Test endpoint with resilience simulation",
            description = "Use this endpoint to test resilience patterns")
    public ResponseEntity<String> testEndpoint() throws InterruptedException {
        int count = requestCount.incrementAndGet();
        log.info("Test endpoint called - request #{}", count);

        // Simulate slow response
        if (simulateSlowResponse.get()) {
            int delay = slowResponseDelay.get();
            log.warn("Simulating slow response - sleeping for {}ms", delay);
            Thread.sleep(delay);
        }

        // Simulate failure
        if (simulateFailure.get()) {
            // Random failures (50% rate)
            if (random.nextBoolean()) {
                log.error("Simulating failure - returning 500 error");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Simulated failure");
            }
        }

        return ResponseEntity.ok("Success - request #" + count);
    }

    @GetMapping("/circuit-breaker-trigger")
    @Operation(summary = "Trigger circuit breaker",
            description = "Fails consistently to trigger circuit breaker")
    public ResponseEntity<String> triggerCircuitBreaker() {
        int count = requestCount.incrementAndGet();
        log.error("Circuit breaker trigger - failing request #{}", count);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Forced failure to trigger circuit breaker - request #" + count);
    }

    @GetMapping("/timeout-trigger")
    @Operation(summary = "Trigger timeout",
            description = "Takes 35 seconds to trigger timeout (configured at 30s)")
    public ResponseEntity<String> triggerTimeout() throws InterruptedException {
        int count = requestCount.incrementAndGet();
        log.warn("Timeout trigger - sleeping for 35 seconds - request #{}", count);
        Thread.sleep(35000); // 35 seconds
        return ResponseEntity.ok("This should have timed out - request #" + count);
    }

    @GetMapping("/rate-limiter-trigger")
    @Operation(summary = "Test endpoint for rate limiter",
            description = "Call this repeatedly to trigger rate limiter (10 requests/second)")
    public ResponseEntity<String> testRateLimiter() {
        int count = requestCount.incrementAndGet();
        log.info("Rate limiter test - request #{}", count);
        return ResponseEntity.ok("Request #" + count + " - Rate limit: 10 requests/second");
    }
    @lombok.Data
    @lombok.Builder
    private static class TestStatus {
        private boolean failureEnabled;
        private boolean slowResponseEnabled;
        private int slowResponseDelay;
        private int requestCount;
    }
}