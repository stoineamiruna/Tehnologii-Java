package com.example.prefschedule.controller;

import com.example.prefschedule.dto.StudentPreferenceRequestDTO;
import com.example.prefschedule.dto.StudentPreferenceResponseDTO;
import com.example.prefschedule.entity.Course;
import com.example.prefschedule.entity.Student;
import com.example.prefschedule.entity.StudentPreference;
import com.example.prefschedule.exception.ResourceNotFoundException;
import com.example.prefschedule.mapper.StudentPreferenceMapper;
import com.example.prefschedule.repository.CourseRepository;
import com.example.prefschedule.repository.StudentRepository;
import com.example.prefschedule.service.StudentPreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/preferences")
public class StudentPreferenceController {

    private final StudentPreferenceService service;
    private final StudentRepository studentRepo;
    private final CourseRepository courseRepo;
    private final StudentPreferenceMapper mapper;

    public StudentPreferenceController(StudentPreferenceService service,
                                       StudentRepository studentRepo,
                                       CourseRepository courseRepo,
                                       StudentPreferenceMapper mapper) {
        this.service = service;
        this.studentRepo = studentRepo;
        this.courseRepo = courseRepo;
        this.mapper = mapper;
    }

    @Operation(summary = "Get all student preferences", description = "Retrieves all student preferences. Requires ROLE_ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of preferences retrieved successfully")
    })
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public List<StudentPreferenceResponseDTO> getAllPreferences() {
        return service.getAll()
                .stream()
                .map(mapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Operation(summary = "Get preferences by student ID", description = "Retrieves all preferences for a specific student. Requires ROLE_ADMIN or ROLE_STUDENT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of student preferences retrieved successfully")
    })
    @GetMapping("/{studentId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STUDENT')")
    public List<StudentPreferenceResponseDTO> getPreferences(@PathVariable Long studentId) {
        List<StudentPreference> prefs = service.getByStudent_Id(studentId);
        return prefs.stream().map(mapper::toResponseDTO).collect(Collectors.toList());
    }

    @Operation(summary = "Get a single preference by ID", description = "Retrieves a single student preference by its unique ID. Requires ROLE_ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Preference retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Preference not found")
    })
    @GetMapping("/preference/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public StudentPreferenceResponseDTO getPreference(@PathVariable Long id) {
        StudentPreference pref = service.getById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Preference not found"));
        return mapper.toResponseDTO(pref);
    }

    @Operation(summary = "Create a new student preference", description = "Creates a new student preference. Requires ROLE_STUDENT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Preference created successfully"),
            @ApiResponse(responseCode = "404", description = "Student or Course not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_STUDENT')")
    public StudentPreferenceResponseDTO createPreference(@Valid @RequestBody StudentPreferenceRequestDTO dto) {
        Student student = studentRepo.findById(dto.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        Course course = courseRepo.findById(dto.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        StudentPreference saved = service.save(mapper.toEntity(dto, student, course));
        return mapper.toResponseDTO(saved);
    }

    @Operation(summary = "Update a student preference", description = "Updates an existing student preference by ID. Requires ROLE_STUDENT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Preference updated successfully"),
            @ApiResponse(responseCode = "404", description = "Preference, Student, or Course not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_STUDENT')")
    public StudentPreferenceResponseDTO updatePreference(@PathVariable Long id,
                                                         @Valid @RequestBody StudentPreferenceRequestDTO dto) {
        StudentPreference existing = service.getById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Preference not found"));

        Student student = studentRepo.findById(dto.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        Course course = courseRepo.findById(dto.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        existing.setStudent(student);
        existing.setCourse(course);
        existing.setPreferenceOrder(dto.getPreferenceOrder());

        StudentPreference updated = service.save(existing);
        return mapper.toResponseDTO(updated);
    }

    @Operation(summary = "Delete a student preference", description = "Deletes a student preference by ID. Requires ROLE_STUDENT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Preference deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Preference not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_STUDENT')")
    public ResponseEntity<Void> deletePreference(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
