package com.example.prefschedule.controller;

import com.example.prefschedule.dto.InstructorRequestDTO;
import com.example.prefschedule.entity.Instructor;
import com.example.prefschedule.exception.ResourceNotFoundException;
import com.example.prefschedule.repository.InstructorRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/instructors")
public class InstructorController {

    private final InstructorRepository repository;

    public InstructorController(InstructorRepository repository) {
        this.repository = repository;
    }

    @Operation(summary = "Get all instructors", description = "Retrieves a list of all instructors. Requires ROLE_ADMIN or ROLE_INSTRUCTOR.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of instructors retrieved successfully")
    })
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_INSTRUCTOR')")
    public List<Instructor> getAllInstructors() {
        return repository.findAll();
    }

    @Operation(summary = "Get instructor by ID", description = "Retrieves a single instructor by their unique ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Instructor retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Instructor not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_INSTRUCTOR')")
    public ResponseEntity<Instructor> getInstructorById(@PathVariable Long id) {
        Instructor instructor = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));
        return ResponseEntity.ok(instructor);
    }

    @Operation(summary = "Create a new instructor", description = "Creates a new instructor. Requires ROLE_ADMIN authority.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Instructor created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<Instructor> createInstructor(@Valid @RequestBody InstructorRequestDTO dto) {
        Instructor instructor = new Instructor();
        instructor.setName(dto.getName());
        instructor.setEmail(dto.getEmail());
        Instructor saved = repository.save(instructor);
        return ResponseEntity.ok(saved);
    }

    @Operation(summary = "Update an existing instructor", description = "Updates an instructor's details by ID. Requires ROLE_ADMIN or ROLE_INSTRUCTOR authority.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Instructor updated successfully"),
            @ApiResponse(responseCode = "404", description = "Instructor not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_INSTRUCTOR')")
    public ResponseEntity<Instructor> updateInstructor(
            @PathVariable Long id,
            @Valid @RequestBody InstructorRequestDTO dto) {

        Instructor existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));
        existing.setName(dto.getName());
        existing.setEmail(dto.getEmail());
        Instructor saved = repository.save(existing);
        return ResponseEntity.ok(saved);
    }

    @Operation(summary = "Update instructor email", description = "Updates only the email of an instructor. Requires ROLE_ADMIN or ROLE_INSTRUCTOR authority.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Instructor email updated successfully"),
            @ApiResponse(responseCode = "404", description = "Instructor not found")
    })
    @PatchMapping("/{id}/email")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_INSTRUCTOR')")
    public ResponseEntity<Instructor> updateEmail(
            @PathVariable Long id,
            @RequestBody String email) {

        Instructor existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));
        existing.setEmail(email);
        repository.save(existing);
        return ResponseEntity.ok(existing);
    }

    @Operation(summary = "Delete instructor", description = "Deletes an instructor by ID. Requires ROLE_ADMIN authority.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Instructor deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Instructor not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteInstructor(@PathVariable Long id) {
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
