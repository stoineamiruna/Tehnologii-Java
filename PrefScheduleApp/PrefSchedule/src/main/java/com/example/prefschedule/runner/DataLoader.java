package com.example.prefschedule.runner;

import com.example.prefschedule.entity.*;
import com.example.prefschedule.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import com.github.javafaker.Faker;

import java.util.List;
import java.util.Set;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private InstructorRepository instructorRepository;
    @Autowired
    private PackRepository packRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;


    @Override
    public void run(String... args) throws Exception {
        /*
        System.out.println("=== Populating DB with Faker ===");

        Faker faker = new Faker();

        // Students
        for (int i = 0; i < 5; i++) {
            String code = "ST" + faker.number().numberBetween(1000, 9999);
            Student student = new Student(code, faker.name().fullName(), faker.internet().emailAddress(), faker.number().numberBetween(1, 3));
            studentRepository.save(student);
        }

        // Instructors
        for (int i = 0; i < 3; i++) {
            Instructor instructor = new Instructor(faker.name().fullName(), faker.internet().emailAddress());
            instructorRepository.save(instructor);
        }

        // Packs
        for (int i = 0; i < 2; i++) {
            Pack pack = new Pack(faker.number().numberBetween(1, 3), faker.number().numberBetween(1, 2), faker.educator().course());
            packRepository.save(pack);
        }

        // Courses
        for (int i = 0; i < 5; i++) {
            Instructor instructor = instructorRepository.findAll().get(faker.number().numberBetween(0, instructorRepository.findAll().size()));
            Pack pack = packRepository.findAll().get(faker.number().numberBetween(0, packRepository.findAll().size()));
            Course course = new Course("optional", faker.code().asin(), faker.educator().campus(), faker.educator().course(), faker.number().numberBetween(1, 5), faker.lorem().sentence(), instructor, pack);
            courseRepository.save(course);
        }
        for (int i = 0; i < 5; i++) {
            Instructor instructor = instructorRepository.findAll().get(faker.number().numberBetween(0, instructorRepository.findAll().size()));
            Pack pack = packRepository.findAll().get(faker.number().numberBetween(0, packRepository.findAll().size()));
            Course course = new Course("compulsory", faker.code().asin(), faker.educator().campus(), faker.educator().course(), faker.number().numberBetween(1, 5), faker.lorem().sentence(), instructor, pack);
            courseRepository.save(course);
        }

        System.out.println("All Students: " + studentRepository.findAll());
        System.out.println("All Instructors: " + instructorRepository.findAll());
        System.out.println("All Packs: " + packRepository.findAll());
        System.out.println("All Courses: " + courseRepository.findAll());

        // Test CRUD
        Course firstCourse = courseRepository.findAll().get(0);
        System.out.println("Before increment: " + firstCourse.getGroupCount());

        //Update
        courseRepository.incrementGroupCount(firstCourse.getId());

        // Fetch
        Course updatedCourse = courseRepository.findById(firstCourse.getId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        System.out.println("After increment: " + updatedCourse.getGroupCount());

         */
        /*
        Role studentRole = roleRepository.findByName("ROLE_STUDENT")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_STUDENT")));
        Role instructorRole = roleRepository.findByName("ROLE_INSTRUCTOR")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_INSTRUCTOR")));
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_ADMIN")));

        List<Student> students = studentRepository.findAll();
        for (Student student : students) {
            // verificăm dacă deja are user asociat
            boolean alreadyExists = userRepository.existsByStudentProfile(student);
            if (!alreadyExists) {
                AppUser user = new AppUser();
                user.setUsername(student.getEmail() != null ? student.getEmail() : student.getCode());
                user.setPassword(passwordEncoder.encode("1234")); // parolă de test
                user.setFullName(student.getName());
                user.setRoles(Set.of(studentRole));
                user.setStudentProfile(student);
                userRepository.save(user);
            }
        }

        List<Instructor> instructors = instructorRepository.findAll();
        for (Instructor instructor : instructors) {
            boolean alreadyExists = userRepository.existsByInstructorProfile(instructor);
            if (!alreadyExists) {
                AppUser user = new AppUser();
                user.setUsername(instructor.getEmail() != null ? instructor.getEmail() : instructor.getName().replace(" ", "."));
                user.setPassword(passwordEncoder.encode("1234"));
                user.setFullName(instructor.getName());
                user.setRoles(Set.of(instructorRole));
                user.setInstructorProfile(instructor);
                userRepository.save(user);
            }
        }

        if (!userRepository.existsByUsername("admin")) {
            AppUser admin = new AppUser();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFullName("System Administrator");
            admin.setRoles(Set.of(adminRole));
            userRepository.save(admin);
        }
        */

        // 2) INDEXES
        jdbcTemplate.execute("""
        CREATE INDEX IF NOT EXISTS idx_instructor_pref_course_id 
        ON instructor_course_preferences(course_id);
        """);

        jdbcTemplate.execute("""
        CREATE INDEX IF NOT EXISTS idx_instructor_pref_compulsory
        ON instructor_course_preferences(compulsory_course_abbr);
        """);

        // 3) COMMENTS
        jdbcTemplate.execute("""
        COMMENT ON TABLE instructor_course_preferences IS 
        'Stores instructor preferences for student selection based on compulsory course grades';
        """);

        jdbcTemplate.execute("""
        COMMENT ON COLUMN instructor_course_preferences.course_id IS 
        'The optional course for which preferences are set';
        """);

        jdbcTemplate.execute("""
        COMMENT ON COLUMN instructor_course_preferences.compulsory_course_abbr IS 
        'Abbreviation of the compulsory course used for grading criteria';
        """);

        jdbcTemplate.execute("""
        COMMENT ON COLUMN instructor_course_preferences.weight_percentage IS 
        'Weight percentage (0-100) for this compulsory course in student ranking';
        """);

        // 4) INSERT DATA (NOW ON CONFLICT WILL WORK!)
        jdbcTemplate.execute("""
        INSERT INTO instructor_course_preferences (course_id, compulsory_course_abbr, weight_percentage)
        SELECT c.id, 'MATH', 100.0 FROM courses c
        WHERE c.code = 'CO1' AND c.type = 'OPTIONAL'
        ON CONFLICT (course_id, compulsory_course_abbr) DO NOTHING;
        """);

        jdbcTemplate.execute("""
        INSERT INTO instructor_course_preferences (course_id, compulsory_course_abbr, weight_percentage)
        SELECT c.id, 'OOP', 50.0 FROM courses c
        WHERE c.code = 'CO2' AND c.type = 'OPTIONAL'
        ON CONFLICT (course_id, compulsory_course_abbr) DO NOTHING;
        """);

        jdbcTemplate.execute("""
        INSERT INTO instructor_course_preferences (course_id, compulsory_course_abbr, weight_percentage)
        SELECT c.id, 'JAVA', 50.0 FROM courses c
        WHERE c.code = 'CO2' AND c.type = 'OPTIONAL'
        ON CONFLICT (course_id, compulsory_course_abbr) DO NOTHING;
        """);
    }
}
