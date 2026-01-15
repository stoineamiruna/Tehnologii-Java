package com.example.prefschedule.integration;

import com.example.prefschedule.config.TestSecurityConfig;
import com.example.prefschedule.entity.Student;
import com.example.prefschedule.repository.StudentRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
@SpringBootTest
@Testcontainers
@Import(TestSecurityConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@WithMockUser(username = "admin", roles = {"ADMIN"}) // Admin pentru toate testele
class StudentIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("eureka.client.enabled", () -> "false");
        registry.add("spring.kafka.enabled", () -> "false");
    }

    @Autowired
    private StudentRepository studentRepository;

    @BeforeEach
    void setUp() {
        studentRepository.deleteAll();
    }

    @Test
    @Order(1)
    @DisplayName("Testcontainers: Verify PostgreSQL container is running")
    void testPostgresContainerIsRunning() {
        assertThat(postgres.isRunning()).isTrue();
        assertThat(postgres.getDatabaseName()).isEqualTo("testdb");
    }

    @Test
    @Order(2)
    @DisplayName("Testcontainers: Verify DB schema is created automatically")
    void testDatabaseSchemaCreated() {
        List<Student> students = studentRepository.findAll();
        assertThat(students).isNotNull();
        assertThat(students).isEmpty();
    }

    @Test
    @Order(3)
    @DisplayName("CRUD: Create student")
    void testCreateStudent() {
        Student student = new Student();
        student.setCode("STU001");
        student.setName("Ion Popescu");
        student.setEmail("ion.popescu@test.ro");
        student.setYear(3);

        Student saved = studentRepository.save(student);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCode()).isEqualTo("STU001");
        assertThat(saved.getName()).isEqualTo("Ion Popescu");
        assertThat(saved.getEmail()).isEqualTo("ion.popescu@test.ro");
        assertThat(saved.getYear()).isEqualTo(3);
    }

    @Test
    @Order(4)
    @DisplayName("CRUD: Read student by ID")
    void testReadStudent() {
        Student student = new Student();
        student.setCode("STU002");
        student.setName("Maria Ionescu");
        student.setEmail("maria@test.ro");
        student.setYear(2);
        Student saved = studentRepository.save(student);

        Optional<Student> found = studentRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getCode()).isEqualTo("STU002");
        assertThat(found.get().getName()).isEqualTo("Maria Ionescu");
    }

    @Test
    @Order(5)
    @DisplayName("CRUD: Update student")
    void testUpdateStudent() {
        Student student = new Student();
        student.setCode("STU003");
        student.setName("Andrei Pop");
        student.setEmail("andrei@test.ro");
        student.setYear(1);
        Student saved = studentRepository.save(student);

        saved.setName("Andrei Pop Updated");
        saved.setEmail("andrei.updated@test.ro");
        saved.setYear(2);
        Student updated = studentRepository.save(saved);

        assertThat(updated.getId()).isEqualTo(saved.getId());
        assertThat(updated.getName()).isEqualTo("Andrei Pop Updated");
        assertThat(updated.getEmail()).isEqualTo("andrei.updated@test.ro");
        assertThat(updated.getYear()).isEqualTo(2);
    }

    @Test
    @Order(6)
    @DisplayName("CRUD: Delete student")
    void testDeleteStudent() {
        Student student = new Student();
        student.setCode("STU004");
        student.setName("Elena Radu");
        student.setEmail("elena@test.ro");
        student.setYear(4);
        Student saved = studentRepository.save(student);
        Long studentId = saved.getId();

        studentRepository.deleteById(studentId);

        Optional<Student> deleted = studentRepository.findById(studentId);
        assertThat(deleted).isEmpty();
    }

    @Test
    @Order(7)
    @DisplayName("CRUD: Find all students")
    void testFindAllStudents() {
        Student student1 = new Student("STU005", "John Doe", "john@test.ro", 1);
        Student student2 = new Student("STU006", "Jane Doe", "jane@test.ro", 2);
        Student student3 = new Student("STU007", "Bob Smith", "bob@test.ro", 3);

        studentRepository.saveAll(List.of(student1, student2, student3));

        List<Student> allStudents = studentRepository.findAll();

        assertThat(allStudents).hasSize(3);
        assertThat(allStudents).extracting(Student::getCode)
                .containsExactlyInAnyOrder("STU005", "STU006", "STU007");
    }

    @Test
    @Order(8)
    @DisplayName("CRUD: Find students by year")
    void testFindStudentsByYear() {
        Student student1 = new Student("STU008", "Alice", "alice@test.ro", 2);
        Student student2 = new Student("STU009", "Charlie", "charlie@test.ro", 2);
        Student student3 = new Student("STU010", "David", "david@test.ro", 3);

        studentRepository.saveAll(List.of(student1, student2, student3));

        List<Student> year2Students = studentRepository.findByYear(2);

        assertThat(year2Students).hasSize(2);
        assertThat(year2Students).extracting(Student::getName)
                .containsExactlyInAnyOrder("Alice", "Charlie");
    }

    @Test
    @Order(9)
    @DisplayName("CRUD: Test unique constraint on code")
    void testUniqueCodeConstraint() {
        Student student1 = new Student("UNIQUE001", "First Student", "first@test.ro", 1);
        studentRepository.save(student1);

        Student student2 = new Student("UNIQUE001", "Second Student", "second@test.ro", 2);

        Assertions.assertThrows(Exception.class, () -> {
            studentRepository.save(student2);
            studentRepository.flush();
        });
    }

    @AfterAll
    static void tearDown() {
        postgres.stop();
    }
}