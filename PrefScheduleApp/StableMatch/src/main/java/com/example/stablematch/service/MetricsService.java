package com.example.stablematch.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Service for managing application metrics using Micrometer
 */
@Service
@Slf4j
public class MetricsService {

    private final MeterRegistry meterRegistry;
    private final Counter stableMatchInvocationCounter;
    private final Counter randomMatchInvocationCounter;
    private final Timer stableMatchTimer;
    private final Timer randomMatchTimer;

    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Create counter for stable matching algorithm invocations
        this.stableMatchInvocationCounter = Counter.builder("stablematch.algorithm.invocations")
                .description("Number of times the stable matching algorithm is invoked")
                .tag("algorithm", "stable")
                .register(meterRegistry);

        // Create counter for random matching algorithm invocations
        this.randomMatchInvocationCounter = Counter.builder("stablematch.algorithm.invocations")
                .description("Number of times the random matching algorithm is invoked")
                .tag("algorithm", "random")
                .register(meterRegistry);

        // Create timer for stable matching algorithm response time
        this.stableMatchTimer = Timer.builder("stablematch.algorithm.response.time")
                .description("Response time of the stable matching algorithm")
                .tag("algorithm", "stable")
                .register(meterRegistry);

        // Create timer for random matching algorithm response time
        this.randomMatchTimer = Timer.builder("stablematch.algorithm.response.time")
                .description("Response time of the random matching algorithm")
                .tag("algorithm", "random")
                .register(meterRegistry);

        log.info("Metrics service initialized with counters and timers");
    }

    /**
     * Increment the stable matching invocation counter
     */
    public void incrementStableMatchCounter() {
        stableMatchInvocationCounter.increment();
        log.debug("Stable match counter incremented. Current count: {}",
                stableMatchInvocationCounter.count());
    }

    /**
     * Increment the random matching invocation counter
     */
    public void incrementRandomMatchCounter() {
        randomMatchInvocationCounter.increment();
        log.debug("Random match counter incremented. Current count: {}",
                randomMatchInvocationCounter.count());
    }

    /**
     * Record execution time for stable matching algorithm
     */
    public void recordStableMatchTime(long duration, TimeUnit unit) {
        stableMatchTimer.record(duration, unit);
        log.debug("Stable match execution time recorded: {} {}", duration, unit);
    }

    /**
     * Record execution time for random matching algorithm
     */
    public void recordRandomMatchTime(long duration, TimeUnit unit) {
        randomMatchTimer.record(duration, unit);
        log.debug("Random match execution time recorded: {} {}", duration, unit);
    }

    /**
     * Execute and measure stable matching algorithm
     */
    public <T> T recordStableMatch(Timer.Sample sample, Runnable task) {
        try {
            incrementStableMatchCounter();
            task.run();
            sample.stop(stableMatchTimer);
            return null;
        } catch (Exception e) {
            log.error("Error during stable matching execution", e);
            throw e;
        }
    }

    /**
     * Get current counter value for stable matching
     */
    public double getStableMatchCount() {
        return stableMatchInvocationCounter.count();
    }

    /**
     * Get current counter value for random matching
     */
    public double getRandomMatchCount() {
        return randomMatchInvocationCounter.count();
    }

    /**
     * Get mean response time for stable matching (in milliseconds)
     */
    public double getStableMatchMeanTime() {
        return stableMatchTimer.mean(TimeUnit.MILLISECONDS);
    }

    /**
     * Get max response time for stable matching (in milliseconds)
     */
    public double getStableMatchMaxTime() {
        return stableMatchTimer.max(TimeUnit.MILLISECONDS);
    }
}