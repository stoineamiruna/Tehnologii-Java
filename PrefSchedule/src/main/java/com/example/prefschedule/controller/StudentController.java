package com.example.prefschedule.controller;

import com.example.prefschedule.dto.StudentRequestDTO;
import com.example.prefschedule.dto.StudentResponseDTO;
import com.example.prefschedule.entity.Student;
import com.example.prefschedule.exception.ResourceNotFoundException;
import com.example.prefschedule.mapper.StudentMapper;
import com.example.prefschedule.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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

    @GetMapping
    public List<StudentResponseDTO> getAllStudents() {
        return studentService.getAll()
                .stream()
                .map(mapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<StudentResponseDTO> createStudent(@Valid @RequestBody StudentRequestDTO dto) {
        Student student = mapper.toEntity(dto);
        Student saved = studentService.save(student);
        return ResponseEntity.ok(mapper.toResponseDTO(saved));
    }

    @PatchMapping("/{id}/email")
    public ResponseEntity<StudentResponseDTO> updateEmail(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        String newEmail = body.get("email");
        studentService.updateEmail(id, newEmail);
        Student updated = studentService.getById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        return ResponseEntity.ok(mapper.toResponseDTO(updated));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentResponseDTO> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody StudentRequestDTO dto) {

        Student updatedEntity = mapper.toEntity(dto);
        Student saved = studentService.updateStudent(id, updatedEntity);
        return ResponseEntity.ok(mapper.toResponseDTO(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
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
