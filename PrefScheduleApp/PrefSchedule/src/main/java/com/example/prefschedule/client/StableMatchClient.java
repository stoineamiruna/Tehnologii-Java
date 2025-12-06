package com.example.prefschedule.client;

import com.example.prefschedule.dto.matching.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Component
@Slf4j
public class StableMatchClient {

    private final WebClient webClient;
    private final Executor taskExecutor;

    public StableMatchClient(
            @Value("${stablematch.service.url}") String baseUrl,
            @Qualifier("taskExecutor") Executor taskExecutor) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
        this.taskExecutor = taskExecutor;
    }

    @CircuitBreaker(name = "stableMatchService", fallbackMethod = "createRandomMatchingFallback")
    @Retry(name = "stableMatchService")
    @TimeLimiter(name = "stableMatchService")
    public CompletableFuture<MatchingResponseDTO> createStableMatching(MatchingRequestDTO request) {
        log.info("Calling StableMatch service for stable matching");

        return webClient.post()
                .uri("/api/matching/stable")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(MatchingResponseDTO.class)
                .timeout(Duration.ofSeconds(30))
                .doOnSuccess(response -> log.info("Successfully received stable matching response"))
                .doOnError(error -> log.error("Error calling StableMatch service", error))
                .toFuture();
    }

    @CircuitBreaker(name = "stableMatchService", fallbackMethod = "createRandomMatchingLocalFallback")
    @Retry(name = "stableMatchService")
    @TimeLimiter(name = "stableMatchService")
    public CompletableFuture<MatchingResponseDTO> createRandomMatching(MatchingRequestDTO request) {
        log.info("Calling StableMatch service for random matching");

        return webClient.post()
                .uri("/api/matching/random")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(MatchingResponseDTO.class)
                .timeout(Duration.ofSeconds(30))
                .doOnSuccess(response -> log.info("Successfully received random matching response"))
                .doOnError(error -> log.error("Error calling StableMatch service", error))
                .toFuture();
    }

    public CompletableFuture<List<AssignmentDTO>> getAllAssignments() {
        log.info("Fetching all assignments from StableMatch service");

        return webClient.get()
                .uri("/api/matching/assignments")
                .retrieve()
                .bodyToFlux(AssignmentDTO.class)
                .collectList()
                .toFuture();
    }

    public CompletableFuture<AssignmentDTO> getAssignmentForStudent(String studentCode) {
        log.info("Fetching assignment for student: {}", studentCode);

        return webClient.get()
                .uri("/api/matching/assignments/student/{studentCode}", studentCode)
                .retrieve()
                .bodyToMono(AssignmentDTO.class)
                .toFuture();
    }
    private CompletableFuture<MatchingResponseDTO> createRandomMatchingFallback(
            MatchingRequestDTO request, Exception ex) {
        log.warn("Stable matching failed, falling back to random matching. Error: {}", ex.getMessage());
        return createRandomMatching(request);
    }
    private CompletableFuture<MatchingResponseDTO> createRandomMatchingLocalFallback(
            MatchingRequestDTO request, Exception ex) {
        log.error("All matching attempts failed, returning empty response. Error: {}", ex.getMessage());

        MatchingResponseDTO fallbackResponse = MatchingResponseDTO.builder()
                .assignments(List.of())
                .statistics(MatchingStatisticsDTO.builder()
                        .totalStudents(request.getStudentPreferences().size())
                        .assignedStudents(0)
                        .unassignedStudents(request.getStudentPreferences().size())
                        .averagePreferenceRank(0.0)
                        .build())
                .build();

        return CompletableFuture.completedFuture(fallbackResponse);
    }
}