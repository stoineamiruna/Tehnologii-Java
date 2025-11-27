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

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public List<StudentPreferenceResponseDTO> getAllPreferences() {
        return service.getAll()
                .stream()
                .map(mapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{studentId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STUDENT')")
    public List<StudentPreferenceResponseDTO> getPreferences(@PathVariable Long studentId) {
        List<StudentPreference> prefs = service.getByStudent_Id(studentId);
        return prefs.stream().map(mapper::toResponseDTO).collect(Collectors.toList());
    }

    @GetMapping("/preference/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public StudentPreferenceResponseDTO getPreference(@PathVariable Long id) {
        StudentPreference pref = service.getById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Preference not found"));
        return mapper.toResponseDTO(pref);
    }

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

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_STUDENT')")
    public ResponseEntity<Void> deletePreference(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
