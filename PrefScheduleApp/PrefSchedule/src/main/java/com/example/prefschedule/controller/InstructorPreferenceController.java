package com.example.prefschedule.controller;

import com.example.prefschedule.dto.InstructorCoursePreferenceDTO;
import com.example.prefschedule.dto.InstructorPreferencesRequest;
import com.example.prefschedule.dto.InstructorPreferencesResponse;
import com.example.prefschedule.service.InstructorCoursePreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/instructor-preferences")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Instructor Preferences", description = "Manage instructor course preferences")
@SecurityRequirement(name = "bearerAuth")
public class InstructorPreferenceController {

    private final InstructorCoursePreferenceService preferenceService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_INSTRUCTOR')")
    @Operation(summary = "Set preferences for a course",
            description = "Sets the instructor's preferences for student selection based on compulsory course grades")
    public ResponseEntity<InstructorPreferencesResponse> setPreferences(
            @Valid @RequestBody InstructorPreferencesRequest request) {
        log.info("Setting preferences for course ID: {}", request.getCourseId());
        InstructorPreferencesResponse response = preferenceService.setPreferencesForCourse(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/course/{courseId}")
    @Operation(summary = "Get preferences for a course",
            description = "Retrieves the instructor's preferences for a specific course")
    public ResponseEntity<InstructorPreferencesResponse> getPreferencesForCourse(
            @PathVariable Long courseId) {
        InstructorPreferencesResponse response = preferenceService.getPreferencesForCourse(courseId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/instructor/{instructorId}")
    @Operation(summary = "Get all preferences for an instructor",
            description = "Retrieves all course preferences set by a specific instructor")
    public ResponseEntity<List<InstructorPreferencesResponse>> getPreferencesForInstructor(
            @PathVariable Long instructorId) {
        List<InstructorPreferencesResponse> responses = preferenceService.getPreferencesForInstructor(instructorId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    @Operation(summary = "Get all preferences",
            description = "Retrieves all instructor preferences (Admin only)")
    public ResponseEntity<List<InstructorCoursePreferenceDTO>> getAllPreferences() {
        List<InstructorCoursePreferenceDTO> preferences = preferenceService.getAllPreferences();
        return ResponseEntity.ok(preferences);
    }

    @DeleteMapping("/course/{courseId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_INSTRUCTOR')")
    @Operation(summary = "Delete preferences for a course",
            description = "Deletes all preferences for a specific course")
    public ResponseEntity<Void> deletePreferencesForCourse(@PathVariable Long courseId) {
        log.info("Deleting preferences for course ID: {}", courseId);
        preferenceService.deletePreferencesForCourse(courseId);
        return ResponseEntity.noContent().build();
    }
}