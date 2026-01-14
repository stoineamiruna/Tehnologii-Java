package com.example.prefschedule.controller;

import com.example.prefschedule.dto.InstructorRequestDTO;
import com.example.prefschedule.entity.Instructor;
import com.example.prefschedule.exception.ResourceNotFoundException;
import com.example.prefschedule.repository.InstructorRepository;
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

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_INSTRUCTOR')")
    public List<Instructor> getAllInstructors() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_INSTRUCTOR')")
    public ResponseEntity<Instructor> getInstructorById(@PathVariable Long id) {
        Instructor instructor = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));
        return ResponseEntity.ok(instructor);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<Instructor> createInstructor(@Valid @RequestBody InstructorRequestDTO dto) {
        Instructor instructor = new Instructor();
        instructor.setName(dto.getName());
        instructor.setEmail(dto.getEmail());
        Instructor saved = repository.save(instructor);
        return ResponseEntity.ok(saved);
    }

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

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteInstructor(@PathVariable Long id) {
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
