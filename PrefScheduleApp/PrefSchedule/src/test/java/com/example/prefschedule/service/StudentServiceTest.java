package com.example.prefschedule.service;

import com.example.prefschedule.controller.StudentController;
import com.example.prefschedule.entity.Student;
import com.example.prefschedule.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for StudentService.
 * Uses Mockito to mock the StudentRepository dependency.
 */
@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentService studentService;

    private Student testStudent;

    @BeforeEach
    void setUp() {
        // Creăm un student de test pentru fiecare test
        testStudent = new Student();
        testStudent.setId(1L);
        testStudent.setCode("STU001");
        testStudent.setName("Ion Popescu");
        testStudent.setEmail("ion.popescu@student.ro");
        testStudent.setYear(3);
    }

    // ============ HAPPY PATH TESTS ============

    @Test
    @DisplayName("Happy Path: Should return all students")
    void testGetAll_Success() {
        // Given - Pregătim datele de test
        Student student2 = new Student();
        student2.setId(2L);
        student2.setCode("STU002");
        student2.setName("Maria Ionescu");
        student2.setEmail("maria.ionescu@student.ro");
        student2.setYear(2);

        List<Student> mockStudents = Arrays.asList(testStudent, student2);

        // Mockăm comportamentul repository-ului
        when(studentRepository.findAll()).thenReturn(mockStudents);

        // When - Apelăm metoda de testat
        List<Student> result = studentService.getAll();

        // Then - Verificăm rezultatele
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(testStudent, student2);

        // Verificăm că repository-ul a fost apelat exact o dată
        verify(studentRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Happy Path: Should save student successfully")
    void testSave_Success() {
        // Given
        when(studentRepository.save(any(Student.class))).thenReturn(testStudent);

        // When
        Student savedStudent = studentService.save(testStudent);

        // Then
        assertThat(savedStudent).isNotNull();
        assertThat(savedStudent.getId()).isEqualTo(1L);
        assertThat(savedStudent.getName()).isEqualTo("Ion Popescu");
        assertThat(savedStudent.getCode()).isEqualTo("STU001");

        verify(studentRepository, times(1)).save(testStudent);
    }

    @Test
    @DisplayName("Happy Path: Should find student by ID")
    void testGetById_Success() {
        // Given
        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));

        // When
        Optional<Student> result = studentService.getById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getName()).isEqualTo("Ion Popescu");

        verify(studentRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Happy Path: Should update student successfully")
    void testUpdateStudent_Success() {
        // Given
        Student updatedData = new Student();
        updatedData.setCode("STU001-UPDATED");
        updatedData.setName("Ion Popescu Updated");
        updatedData.setEmail("ion.updated@student.ro");
        updatedData.setYear(4);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Student result = studentService.updateStudent(1L, updatedData);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Ion Popescu Updated");
        assertThat(result.getCode()).isEqualTo("STU001-UPDATED");
        assertThat(result.getEmail()).isEqualTo("ion.updated@student.ro");
        assertThat(result.getYear()).isEqualTo(4);

        verify(studentRepository, times(1)).findById(1L);
        verify(studentRepository, times(1)).save(any(Student.class));
    }

    // ============ ERROR SCENARIO TESTS ============

    @Test
    @DisplayName("Error: Should throw exception when student not found for update")
    void testUpdateStudent_NotFound_ThrowsException() {
        // Given
        Student updatedData = new Student();
        updatedData.setName("New Name");

        when(studentRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> studentService.updateStudent(999L, updatedData))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Student not found");

        verify(studentRepository, times(1)).findById(999L);
        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    @DisplayName("Error: Should return empty when student ID doesn't exist")
    void testGetById_NotFound_ReturnsEmpty() {
        // Given
        when(studentRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Student> result = studentService.getById(999L);

        // Then
        assertThat(result).isEmpty();
        verify(studentRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Error: Should handle repository exception during save")
    void testSave_RepositoryException_ThrowsException() {
        // Given
        when(studentRepository.save(any(Student.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        assertThatThrownBy(() -> studentService.save(testStudent))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Database connection failed");

        verify(studentRepository, times(1)).save(testStudent);
    }

    @Test
    @DisplayName("Happy Path: Should delete student by ID")
    void testDeleteById_Success() {
        // Given
        doNothing().when(studentRepository).deleteById(1L);

        // When
        studentService.deleteById(1L);

        // Then
        verify(studentRepository, times(1)).deleteById(1L);
    }
}