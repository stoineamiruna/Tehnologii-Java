package com.example.prefschedule.integration;

import com.example.prefschedule.entity.Student;
import com.example.prefschedule.repository.StudentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests using Testcontainers.
 * Verifies that:
 * 1. DB schema is created automatically
 * 2. CRUD operations work correctly with real database
 */
@SpringBootTest
@Testcontainers
class StudentIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private StudentRepository studentRepository;

    @Test
    @DisplayName("Integration Test: DB Schema should be created automatically")
    void testDatabaseSchemaCreation() {
        // Given - Container-ul PostgreSQL rulează
        assertThat(postgres.isRunning()).isTrue();

        // When - Repository-ul este injectat
        assertThat(studentRepository).isNotNull();

        // Then - Putem executa operații pe DB (schema există)
        long count = studentRepository.count();
        assertThat(count).isEqualTo(0); // DB-ul este gol la început
    }

    @Test
    @DisplayName("Integration Test: CREATE - Should save student to database")
    void testCreateStudent() {
        // Given
        Student student = new Student();
        student.setCode("STU001");
        student.setName("Ion Popescu");
        student.setEmail("ion.popescu@student.ro");
        student.setYear(3);

        // When
        Student savedStudent = studentRepository.save(student);

        // Then
        assertThat(savedStudent.getId()).isNotNull();
        assertThat(savedStudent.getCode()).isEqualTo("STU001");
        assertThat(savedStudent.getName()).isEqualTo("Ion Popescu");
        assertThat(savedStudent.getEmail()).isEqualTo("ion.popescu@student.ro");
        assertThat(savedStudent.getYear()).isEqualTo(3);
    }

    @Test
    @DisplayName("Integration Test: READ - Should retrieve student from database")
    void testReadStudent() {
        // Given - Salvăm un student
        Student student = new Student();
        student.setCode("STU002");
        student.setName("Maria Ionescu");
        student.setEmail("maria@student.ro");
        student.setYear(2);
        Student savedStudent = studentRepository.save(student);

        // When - Citim studentul după ID
        Optional<Student> retrievedStudent = studentRepository.findById(savedStudent.getId());

        // Then
        assertThat(retrievedStudent).isPresent();
        assertThat(retrievedStudent.get().getCode()).isEqualTo("STU002");
        assertThat(retrievedStudent.get().getName()).isEqualTo("Maria Ionescu");
    }

    @Test
    @DisplayName("Integration Test: UPDATE - Should update student in database")
    void testUpdateStudent() {
        // Given - Salvăm un student
        Student student = new Student();
        student.setCode("STU003");
        student.setName("Andrei Georgescu");
        student.setEmail("andrei@student.ro");
        student.setYear(1);
        Student savedStudent = studentRepository.save(student);

        // When - Actualizăm datele
        savedStudent.setName("Andrei Georgescu Updated");
        savedStudent.setYear(2);
        studentRepository.save(savedStudent);

        // Then - Verificăm că modificările au fost salvate
        Optional<Student> updatedStudent = studentRepository.findById(savedStudent.getId());
        assertThat(updatedStudent).isPresent();
        assertThat(updatedStudent.get().getName()).isEqualTo("Andrei Georgescu Updated");
        assertThat(updatedStudent.get().getYear()).isEqualTo(2);
    }

    @Test
    @DisplayName("Integration Test: DELETE - Should delete student from database")
    void testDeleteStudent() {
        // Given - Salvăm un student
        Student student = new Student();
        student.setCode("STU004");
        student.setName("Elena Vasilescu");
        student.setEmail("elena@student.ro");
        student.setYear(4);
        Student savedStudent = studentRepository.save(student);
        Long studentId = savedStudent.getId();

        // When - Ștergem studentul
        studentRepository.deleteById(studentId);

        // Then - Verificăm că studentul nu mai există
        Optional<Student> deletedStudent = studentRepository.findById(studentId);
        assertThat(deletedStudent).isEmpty();
    }

    @Test
    @DisplayName("Integration Test: LIST - Should retrieve all students")
    void testListAllStudents() {
        // Given - Ștergem datele existente și adăugăm studenți noi
        studentRepository.deleteAll();

        Student student1 = new Student();
        student1.setCode("STU005");
        student1.setName("Student 1");
        student1.setEmail("student1@student.ro");
        student1.setYear(1);

        Student student2 = new Student();
        student2.setCode("STU006");
        student2.setName("Student 2");
        student2.setEmail("student2@student.ro");
        student2.setYear(2);

        studentRepository.save(student1);
        studentRepository.save(student2);

        // When
        List<Student> allStudents = studentRepository.findAll();

        // Then
        assertThat(allStudents).hasSize(2);
        assertThat(allStudents).extracting(Student::getCode)
                .containsExactlyInAnyOrder("STU005", "STU006");
    }
}