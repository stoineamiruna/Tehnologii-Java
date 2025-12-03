package com.example.stablematch.controller;

import com.example.stablematch.dto.AssignmentDTO;
import com.example.stablematch.dto.MatchingRequestDTO;
import com.example.stablematch.dto.MatchingResponseDTO;
import com.example.stablematch.service.MatchingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Matching", description = "Stable matching algorithm endpoints")
public class MatchingController {

    private final MatchingService matchingService;
    private MatchingResponseDTO lastMatchingResult;

    @PostMapping("/stable")
    @Operation(summary = "Create stable matching",
            description = "Creates a stable matching between students and courses using Gale-Shapley algorithm")
    public ResponseEntity<MatchingResponseDTO> createStableMatching(
            @Valid @RequestBody MatchingRequestDTO request) {
        log.info("Received stable matching request for {} students",
                request.getStudentPreferences().size());

        MatchingResponseDTO response = matchingService.createStableMatching(request);
        lastMatchingResult = response;

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/random")
    @Operation(summary = "Create random matching",
            description = "Creates a random matching between students and courses")
    public ResponseEntity<MatchingResponseDTO> createRandomMatching(
            @Valid @RequestBody MatchingRequestDTO request) {
        log.info("Received random matching request for {} students",
                request.getStudentPreferences().size());

        MatchingResponseDTO response = matchingService.createRandomMatching(request);
        lastMatchingResult = response;

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/assignments")
    @Operation(summary = "Get all assignments",
            description = "Returns all student-course assignments from the last matching")
    public ResponseEntity<List<AssignmentDTO>> getAllAssignments() {
        if (lastMatchingResult == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(lastMatchingResult.getAssignments());
    }

    @GetMapping("/assignments/student/{studentCode}")
    @Operation(summary = "Get assignment for a student",
            description = "Returns the course assignment for a specific student")
    public ResponseEntity<AssignmentDTO> getAssignmentForStudent(
            @PathVariable String studentCode) {
        if (lastMatchingResult == null) {
            return ResponseEntity.noContent().build();
        }

        AssignmentDTO assignment = lastMatchingResult.getAssignments().stream()
                .filter(a -> a.getStudentCode().equals(studentCode))
                .findFirst()
                .orElse(null);

        if (assignment == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(assignment);
    }

    @GetMapping("/assignments/course/{courseCode}")
    @Operation(summary = "Get assignments for a course",
            description = "Returns all student assignments for a specific course")
    public ResponseEntity<List<AssignmentDTO>> getAssignmentsForCourse(
            @PathVariable String courseCode) {
        if (lastMatchingResult == null) {
            return ResponseEntity.noContent().build();
        }

        List<AssignmentDTO> assignments = lastMatchingResult.getAssignments().stream()
                .filter(a -> a.getCourseCode().equals(courseCode))
                .collect(Collectors.toList());

        if (assignments.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get matching statistics",
            description = "Returns statistics about the last matching")
    public ResponseEntity<MatchingResponseDTO> getMatchingStatistics() {
        if (lastMatchingResult == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(lastMatchingResult);
    }

    @DeleteMapping("/clear")
    @Operation(summary = "Clear matching results",
            description = "Clears the stored matching results")
    public ResponseEntity<Void> clearMatchingResults() {
        lastMatchingResult = null;
        log.info("Cleared matching results");
        return ResponseEntity.noContent().build();
    }
}