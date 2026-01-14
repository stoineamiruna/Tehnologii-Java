package com.example.prefschedule.controller;

import com.example.prefschedule.dto.StudentRequestDTO;
import com.example.prefschedule.dto.StudentResponseDTO;
import com.example.prefschedule.entity.Student;
import com.example.prefschedule.exception.ResourceNotFoundException;
import com.example.prefschedule.mapper.StudentMapper;
import com.example.prefschedule.service.StudentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.example.prefschedule.exception.GlobalExceptionHandler;
/**
 * Unit tests for StudentController.
 * Uses @WebMvcTest to test only the web layer.
 * Uses @MockBean to mock service dependencies.
 * Security is disabled for testing purposes.
 */
@WebMvcTest(controllers = StudentController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
        })
@Import(GlobalExceptionHandler.class)
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentService studentService;

    @MockBean
    private StudentMapper studentMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private Student testStudent;
    private StudentRequestDTO requestDTO;
    private StudentResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        // Setup test data
        testStudent = new Student();
        testStudent.setId(1L);
        testStudent.setCode("STU001");
        testStudent.setName("Ion Popescu");
        testStudent.setEmail("ion.popescu@student.ro");
        testStudent.setYear(3);

        requestDTO = new StudentRequestDTO();
        requestDTO.setCode("STU001");
        requestDTO.setName("Ion Popescu");
        requestDTO.setEmail("ion.popescu@student.ro");
        requestDTO.setYear(3);

        responseDTO = new StudentResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setCode("STU001");
        responseDTO.setName("Ion Popescu");
        responseDTO.setEmail("ion.popescu@student.ro");
        responseDTO.setYear(3);
    }

    // ============ HAPPY PATH TESTS ============

    @Test
    @DisplayName("Happy Path: GET /api/students should return all students")
    void testGetAllStudents_Success() throws Exception {
        // Given
        StudentResponseDTO responseDTO2 = new StudentResponseDTO();
        responseDTO2.setId(2L);
        responseDTO2.setCode("STU002");
        responseDTO2.setName("Maria Ionescu");
        responseDTO2.setEmail("maria@student.ro");
        responseDTO2.setYear(2);

        List<Student> students = Arrays.asList(testStudent, new Student());
        when(studentService.getAll()).thenReturn(students);
        when(studentMapper.toResponseDTO(any(Student.class)))
                .thenReturn(responseDTO, responseDTO2);

        // When & Then
        mockMvc.perform(get("/api/students")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Ion Popescu")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Maria Ionescu")));

        verify(studentService, times(1)).getAll();
        verify(studentMapper, times(2)).toResponseDTO(any(Student.class));
    }

    @Test
    @DisplayName("Happy Path: POST /api/students should create student")
    void testCreateStudent_Success() throws Exception {
        // Given
        when(studentMapper.toEntity(any(StudentRequestDTO.class))).thenReturn(testStudent);
        when(studentService.save(any(Student.class))).thenReturn(testStudent);
        when(studentMapper.toResponseDTO(any(Student.class))).thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Ion Popescu")))
                .andExpect(jsonPath("$.code", is("STU001")))
                .andExpect(jsonPath("$.email", is("ion.popescu@student.ro")));

        verify(studentMapper, times(1)).toEntity(any(StudentRequestDTO.class));
        verify(studentService, times(1)).save(any(Student.class));
        verify(studentMapper, times(1)).toResponseDTO(any(Student.class));
    }

    @Test
    @DisplayName("Happy Path: GET /api/students/{id} should return student")
    void testGetStudentById_Success() throws Exception {
        // Given
        when(studentService.getById(1L)).thenReturn(Optional.of(testStudent));
        when(studentMapper.toResponseDTO(any(Student.class))).thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(get("/api/students/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Ion Popescu")))
                .andExpect(jsonPath("$.code", is("STU001")));

        verify(studentService, times(1)).getById(1L);
        verify(studentMapper, times(1)).toResponseDTO(testStudent);
    }

    @Test
    @DisplayName("Happy Path: DELETE /api/students/{id} should delete student")
    void testDeleteStudent_Success() throws Exception {
        // Given
        doNothing().when(studentService).deleteById(1L);

        // When & Then
        mockMvc.perform(delete("/api/students/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(studentService, times(1)).deleteById(1L);
    }

    // ============ ERROR SCENARIO TESTS ============

    @Test
    @DisplayName("Error: GET /api/students/{id} should return 404 when student not found")
    void testGetStudentById_NotFound() throws Exception {
        // Given
        when(studentService.getById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/students/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(studentService, times(1)).getById(999L);
        verify(studentMapper, never()).toResponseDTO(any(Student.class));
    }

    @Test
    @DisplayName("Error: POST /api/students should return 400 for invalid data")
    void testCreateStudent_InvalidData_BadRequest() throws Exception {
        // Given - Request DTO cu date invalide (fără nume)
        StudentRequestDTO invalidDTO = new StudentRequestDTO();
        invalidDTO.setCode("STU001");
        invalidDTO.setYear(3);
        // name is missing - validation should fail if @NotBlank is present

        // When & Then
        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());

        verify(studentService, never()).save(any(Student.class));
    }

    @Test
    @DisplayName("Error: Service throws RuntimeException")
    void testGetAllStudents_ServiceException() throws Exception {
        // Given
        when(studentService.getAll()).thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(get("/api/students")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());

        verify(studentService, times(1)).getAll();
    }
}