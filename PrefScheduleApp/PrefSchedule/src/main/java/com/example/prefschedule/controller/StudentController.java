package com.example.prefschedule.controller;

import com.example.prefschedule.dto.StudentRequestDTO;
import com.example.prefschedule.dto.StudentResponseDTO;
import com.example.prefschedule.entity.Student;
import com.example.prefschedule.exception.ResourceNotFoundException;
import com.example.prefschedule.mapper.StudentMapper;
import com.example.prefschedule.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;
    private final StudentMapper mapper;

    public StudentController(StudentService studentService, StudentMapper mapper) {
        this.studentService = studentService;
        this.mapper = mapper;
    }

    @Operation(summary = "Get all students", description = "Retrieves a list of all students. Requires ROLE_ADMIN or ROLE_INSTRUCTOR.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of students retrieved successfully")
    })
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_INSTRUCTOR')")
    public List<StudentResponseDTO> getAllStudents() {
        System.out.println("Inside getAllStudents, auth: " + SecurityContextHolder.getContext().getAuthentication());
        return studentService.getAll()
                .stream()
                .map(mapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Operation(summary = "Create a new student", description = "Creates a new student. Requires ROLE_ADMIN authority.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Student created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<StudentResponseDTO> createStudent(@Valid @RequestBody StudentRequestDTO dto) {
        Student student = mapper.toEntity(dto);
        Student saved = studentService.save(student);
        return ResponseEntity.ok(mapper.toResponseDTO(saved));
    }

    @Operation(summary = "Update student email", description = "Updates only the email of a student. Requires ROLE_ADMIN or ROLE_STUDENT authority.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Student email updated successfully"),
            @ApiResponse(responseCode = "404", description = "Student not found")
    })
    @PatchMapping("/{id}/email")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_STUDENT')")
    public ResponseEntity<StudentResponseDTO> updateEmail(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        String newEmail = body.get("email");
        studentService.updateEmail(id, newEmail);
        Student updated = studentService.getById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        return ResponseEntity.ok(mapper.toResponseDTO(updated));
    }

    @Operation(summary = "Update student details", description = "Updates the full student details by ID. Requires ROLE_ADMIN or ROLE_STUDENT authority.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Student updated successfully"),
            @ApiResponse(responseCode = "404", description = "Student not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_STUDENT')")
    public ResponseEntity<StudentResponseDTO> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody StudentRequestDTO dto) {

        Student updatedEntity = mapper.toEntity(dto);
        Student saved = studentService.updateStudent(id, updatedEntity);
        return ResponseEntity.ok(mapper.toResponseDTO(saved));
    }

    @Operation(summary = "Delete student", description = "Deletes a student by ID. Requires ROLE_ADMIN authority.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Student deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Student not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get student by ID", description = "Retrieves a student by ID. Returns an ETag for caching. Requires ROLE_ADMIN, ROLE_INSTRUCTOR, or ROLE_STUDENT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Student retrieved successfully"),
            @ApiResponse(responseCode = "304", description = "Student data not modified"),
            @ApiResponse(responseCode = "404", description = "Student not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_INSTRUCTOR', 'ROLE_STUDENT')")
    public ResponseEntity<StudentResponseDTO> getStudentById(
            @PathVariable Long id,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {

        Student student = studentService.getById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        String eTag = "\"" + student.hashCode() + "\"";

        if (ifNoneMatch != null && ifNoneMatch.equals(eTag)) {
            return ResponseEntity.status(304).build();
        }

        return ResponseEntity.ok()
                .eTag(eTag)
                .body(mapper.toResponseDTO(student));
    }
}
