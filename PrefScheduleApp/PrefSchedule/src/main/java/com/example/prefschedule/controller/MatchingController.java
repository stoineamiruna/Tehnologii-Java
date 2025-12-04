package com.example.prefschedule.controller;

import com.example.prefschedule.dto.matching.AssignmentDTO;
import com.example.prefschedule.dto.matching.MatchingResponseDTO;
import com.example.prefschedule.service.MatchingOrchestrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Matching", description = "Student-Course matching endpoints")
@SecurityRequirement(name = "bearerAuth")
public class MatchingController {

    private final MatchingOrchestrationService orchestrationService;

    @PostMapping("/pack/{packId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_INSTRUCTOR')")
    @Operation(summary = "Perform matching for a pack",
            description = "Assigns students to courses in a specific pack using stable matching algorithm")
    public ResponseEntity<MatchingResponseDTO> performMatchingForPack(
            @PathVariable Long packId,
            @RequestParam(defaultValue = "true") boolean useStableAlgorithm) {
        log.info("Performing matching for pack ID: {} using {} algorithm",
                packId,
                useStableAlgorithm ? "stable" : "random");

        try {
            // Execute synchronously to avoid async security context issues
            MatchingResponseDTO response = orchestrationService
                    .performMatchingForPack(packId, useStableAlgorithm)
                    .get(); // Block and wait for result

            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("Error performing matching for pack {}", packId, ex);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/year/{year}/semester/{semester}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_INSTRUCTOR')")
    @Operation(summary = "Perform matching for all packs",
            description = "Assigns students to courses for all packs in a year and semester")
    public ResponseEntity<Map<Long, MatchingResponseDTO>> performMatchingForAllPacks(
            @PathVariable Integer year,
            @PathVariable Integer semester,
            @RequestParam(defaultValue = "true") boolean useStableAlgorithm) {
        log.info("Performing matching for all packs in year {} semester {}", year, semester);

        try {
            // Execute synchronously
            Map<Long, MatchingResponseDTO> results = orchestrationService
                    .performMatchingForAllPacks(year, semester, useStableAlgorithm)
                    .get(); // Block and wait for result

            return ResponseEntity.ok(results);
        } catch (Exception ex) {
            log.error("Error performing matching for year {} semester {}", year, semester, ex);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/health")
    @Operation(summary = "Check matching service health",
            description = "Checks if the StableMatch service is accessible")
    public ResponseEntity<String> checkHealth() {
        return ResponseEntity.ok("Matching service is accessible");
    }
}