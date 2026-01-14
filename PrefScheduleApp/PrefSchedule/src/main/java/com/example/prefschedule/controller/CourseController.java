package com.example.prefschedule.controller;

import com.example.prefschedule.dto.CourseRequestDTO;
import com.example.prefschedule.dto.CourseResponseDTO;
import com.example.prefschedule.entity.Course;
import com.example.prefschedule.entity.Instructor;
import com.example.prefschedule.entity.Pack;
import com.example.prefschedule.entity.Student;
import com.example.prefschedule.exception.ResourceNotFoundException;
import com.example.prefschedule.repository.CourseRepository;
import com.example.prefschedule.repository.InstructorRepository;
import com.example.prefschedule.repository.PackRepository;
import com.example.prefschedule.repository.StudentRepository;
import com.example.prefschedule.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;
    private final InstructorRepository instructorRepository;
    private final PackRepository packRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private CourseRepository courseRepository;


    public CourseController(CourseService courseService,
                            InstructorRepository instructorRepository,
                            PackRepository packRepository) {
        this.courseService = courseService;
        this.instructorRepository = instructorRepository;
        this.packRepository = packRepository;
    }
    @Operation(summary = "Get all courses", description = "Returns a list of all courses available in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of courses retrieved successfully")
    })
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_INSTRUCTOR','ROLE_STUDENT')")
    public List<CourseResponseDTO> getAllCourses() {
        return courseService.getAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Operation(summary = "Get course by ID", description = "Retrieve a single course by its unique ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Course retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Course not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_INSTRUCTOR','ROLE_STUDENT')")
    public ResponseEntity<CourseResponseDTO> getCourseById(@PathVariable Long id) {
        Course course = courseService.getById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        return ResponseEntity.ok(toDTO(course));
    }

    @Operation(summary = "Create a new course", description = "Creates a new course. Requires ROLE_ADMIN or ROLE_INSTRUCTOR authority.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Course created successfully"),
            @ApiResponse(responseCode = "404", description = "Instructor or Pack not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_INSTRUCTOR')")
    public ResponseEntity<CourseResponseDTO> createCourse(@Valid @RequestBody CourseRequestDTO dto) {
        Course course = new Course();
        course.setType(dto.getType());
        course.setCode(dto.getCode());
        course.setAbbr(dto.getAbbr());
        course.setName(dto.getName());
        course.setGroupCount(dto.getGroupCount());
        course.setDescription(dto.getDescription());

        if (dto.getInstructorId() != null) {
            Instructor instructor = instructorRepository.findById(dto.getInstructorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));
            course.setInstructor(instructor);
        }

        if (dto.getPackId() != null) {
            Pack pack = packRepository.findById(dto.getPackId())
                    .orElseThrow(() -> new ResourceNotFoundException("Pack not found"));
            course.setPack(pack);
        }

        Course saved = courseService.save(course);
        return ResponseEntity.ok(toDTO(saved));
    }

    @Operation(summary = "Update an existing course", description = "Updates a course by ID. Requires ROLE_ADMIN or ROLE_INSTRUCTOR authority.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Course updated successfully"),
            @ApiResponse(responseCode = "404", description = "Course, Instructor, or Pack not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_INSTRUCTOR')")
    public ResponseEntity<CourseResponseDTO> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseRequestDTO dto) {

        Course existing = courseService.getById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        existing.setType(dto.getType());
        existing.setCode(dto.getCode());
        existing.setAbbr(dto.getAbbr());
        existing.setName(dto.getName());
        existing.setGroupCount(dto.getGroupCount());
        existing.setDescription(dto.getDescription());

        if (dto.getInstructorId() != null) {
            Instructor instructor = instructorRepository.findById(dto.getInstructorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));
            existing.setInstructor(instructor);
        } else {
            existing.setInstructor(null);
        }

        if (dto.getPackId() != null) {
            Pack pack = packRepository.findById(dto.getPackId())
                    .orElseThrow(() -> new ResourceNotFoundException("Pack not found"));
            existing.setPack(pack);
        } else {
            existing.setPack(null);
        }

        Course saved = courseService.save(existing);
        return ResponseEntity.ok(toDTO(saved));
    }
    @Operation(summary = "Delete a course", description = "Deletes a course by ID. Requires ROLE_ADMIN authority.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Course deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Course not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    @Operation(summary = "Get courses for a student", description = "Returns courses available for a student based on their year.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Courses retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Student not found")
    })
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Course>> getCoursesForStudent(@PathVariable Long studentId) {
        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if (studentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Student student = studentOpt.get();
        List<Course> courses = courseRepository.findByPackYear(student.getYear());

        return ResponseEntity.ok(courses);
    }
    @Operation(summary = "Get courses by instructor", description = "Returns all courses taught by a specific instructor.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Courses retrieved successfully"),
            @ApiResponse(responseCode = "204", description = "No courses found for this instructor")
    })
    @GetMapping("/instructor/{instructorId}")
    public ResponseEntity<List<Course>> getCoursesByInstructor(
            @PathVariable Long instructorId) {

        List<Course> courses = courseService.getCoursesByInstructorId(instructorId);

        if (courses.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        return ResponseEntity.ok(courses);
    }

    private CourseResponseDTO toDTO(Course course) {
        CourseResponseDTO dto = new CourseResponseDTO();
        dto.setId(course.getId());
        dto.setType(course.getType());
        dto.setCode(course.getCode());
        dto.setAbbr(course.getAbbr());
        dto.setName(course.getName());
        dto.setGroupCount(course.getGroupCount());
        dto.setDescription(course.getDescription());
        dto.setInstructorId(course.getInstructor() != null ? course.getInstructor().getId() : null);
        dto.setPackId(course.getPack() != null ? course.getPack().getId() : null);
        return dto;
    }
}
