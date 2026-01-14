package com.example.prefschedule.runner;

import com.example.prefschedule.entity.*;
import com.example.prefschedule.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import com.github.javafaker.Faker;

import java.util.HashSet;
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

    @Override
    public void run(String... args) throws Exception {

        System.out.println("=== Populating DB ===");

        // 1️⃣ Creează rolurile (exact ca în versiunea veche)
        Role studentRole = roleRepository.findByName("ROLE_STUDENT")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_STUDENT")));
        Role instructorRole = roleRepository.findByName("ROLE_INSTRUCTOR")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_INSTRUCTOR")));
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_ADMIN")));

        System.out.println("✓ Roles created:");
        System.out.println("  - ROLE_STUDENT (ID: " + studentRole.getId() + ")");
        System.out.println("  - ROLE_INSTRUCTOR (ID: " + instructorRole.getId() + ")");
        System.out.println("  - ROLE_ADMIN (ID: " + adminRole.getId() + ")");

        Faker faker = new Faker();

        // 2️⃣ Creează studenți dacă nu există deja
        System.out.println("\n=== Creating Students ===");
        if (studentRepository.count() < 20) {
            for (int i = 0; i < 20; i++) {
                String code = "ST" + faker.number().numberBetween(1000, 9999);
                if (!studentRepository.existsByCode(code)) {
                    Student student = new Student(
                            code,
                            faker.name().fullName(),
                            faker.internet().emailAddress(),
                            faker.number().numberBetween(1, 4)
                    );
                    studentRepository.save(student);
                    System.out.println("  ✓ Created student: " + student.getName() + " (Year " + student.getYear() + ")");
                }
            }
        }

        // 3️⃣ Creează instructori dacă nu există deja
        System.out.println("\n=== Creating Instructors ===");
        if (instructorRepository.count() < 20) {
            for (int i = 0; i < 20; i++) {
                String email = faker.internet().emailAddress();
                if (!instructorRepository.existsByEmail(email)) {
                    Instructor instructor = new Instructor(
                            faker.name().fullName(),
                            email
                    );
                    instructorRepository.save(instructor);
                    System.out.println("  ✓ Created instructor: " + instructor.getName());
                }
            }
        }

        // 4️⃣ Creează useri pentru studenți (exact ca în versiunea veche)
        System.out.println("\n=== Creating Student Users ===");
        List<Student> students = studentRepository.findAll();
        for (Student student : students) {
            boolean alreadyExists = userRepository.existsByStudentProfile(student);
            if (!alreadyExists) {
                AppUser user = new AppUser();
                user.setUsername(student.getEmail() != null ? student.getEmail() : student.getCode());
                user.setPassword(passwordEncoder.encode("1234"));
                user.setFullName(student.getName());
                user.setRoles(Set.of(studentRole));
                user.setStudentProfile(student);
                userRepository.save(user);
                System.out.println("  ✓ Created user: " + user.getUsername() + " | Roles: ROLE_STUDENT");
            }
        }

        // 5️⃣ Creează useri pentru instructori (exact ca în versiunea veche)
        System.out.println("\n=== Creating Instructor Users ===");
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
                System.out.println("  ✓ Created user: " + user.getUsername() + " | Roles: ROLE_INSTRUCTOR");
            }
        }

        // 6️⃣ Creează user admin (exact ca în versiunea veche)
        System.out.println("\n=== Creating Admin User ===");
        if (!userRepository.existsByUsername("admin")) {
            AppUser admin = new AppUser();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFullName("System Administrator");
            admin.setRoles(Set.of(adminRole));
            AppUser savedAdmin = userRepository.save(admin);

            System.out.println("  ✓✓✓ Admin user created ✓✓✓");
            System.out.println("    - ID: " + savedAdmin.getId());
            System.out.println("    - Username: " + savedAdmin.getUsername());
            System.out.println("    - Password: admin123");
            System.out.println("    - Full Name: " + savedAdmin.getFullName());
            System.out.println("    - Roles: " + savedAdmin.getRoles());
        } else {
            AppUser existingAdmin = userRepository.findByUsername("admin").get();
            System.out.println("  ✓ Admin already exists:");
            System.out.println("    - ID: " + existingAdmin.getId());
            System.out.println("    - Username: " + existingAdmin.getUsername());
            System.out.println("    - Roles: " + existingAdmin.getRoles());
        }

        // 7️⃣ Creează Pack-uri
        System.out.println("\n=== Creating Packs ===");
        if (packRepository.count() < 10) {
            for (int i = 0; i < 10; i++) {
                Pack pack = new Pack(
                        faker.number().numberBetween(1, 4),
                        faker.number().numberBetween(1, 2),
                        "Pack " + (i + 1)
                );
                packRepository.save(pack);
                System.out.println("  ✓ Created pack: " + pack.getName());
            }
        }

        // 8️⃣ Creează cursuri
        System.out.println("\n=== Creating Courses ===");
        List<Instructor> allInstructors = instructorRepository.findAll();
        List<Pack> packs = packRepository.findAll();

        if (!allInstructors.isEmpty() && !packs.isEmpty() && courseRepository.count() < 30) {
            // Optional courses
            for (int i = 0; i < 15; i++) {
                Instructor instructor = allInstructors.get(faker.number().numberBetween(0, allInstructors.size()));
                Pack pack = packs.get(faker.number().numberBetween(0, packs.size()));
                Course course = new Course(
                        "optional",
                        faker.code().asin(),
                        faker.educator().campus(),
                        faker.educator().course(),
                        faker.number().numberBetween(1, 5),
                        faker.lorem().sentence(),
                        instructor,
                        pack
                );
                courseRepository.save(course);
            }

            // Compulsory courses
            for (int i = 0; i < 15; i++) {
                Instructor instructor = allInstructors.get(faker.number().numberBetween(0, allInstructors.size()));
                Pack pack = packs.get(faker.number().numberBetween(0, packs.size()));
                Course course = new Course(
                        "compulsory",
                        faker.code().asin(),
                        faker.educator().campus(),
                        faker.educator().course(),
                        faker.number().numberBetween(1, 5),
                        faker.lorem().sentence(),
                        instructor,
                        pack
                );
                courseRepository.save(course);
            }
            System.out.println("  ✓ Created 10 courses (5 optional, 5 compulsory)");
        }

        System.out.println("\n=== DB population completed! ===");

        // Verificare finală detaliată
        System.out.println("\n=== Final Verification ===");
        System.out.println("Total users: " + userRepository.count());
        System.out.println("Total students: " + studentRepository.count());
        System.out.println("Total instructors: " + instructorRepository.count());
        System.out.println("Total courses: " + courseRepository.count());
        System.out.println("Total packs: " + packRepository.count());

        System.out.println("\n=== All Users with Roles ===");
        userRepository.findAll().forEach(user -> {
            String roleNames = user.getRoles().stream()
                    .map(Role::getName)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("NO ROLES");

            System.out.println("  [ID=" + user.getId() + "] " +
                    user.getUsername() + " | " +
                    user.getFullName() + " | " +
                    "Roles: " + roleNames);
        });

        // Verificare specifică pentru admin
        System.out.println("\n=== Admin User Detailed Check ===");
        userRepository.findByUsername("admin").ifPresent(admin -> {
            System.out.println("  Username: " + admin.getUsername());
            System.out.println("  ID: " + admin.getId());
            System.out.println("  Full Name: " + admin.getFullName());
            System.out.println("  Roles count: " + admin.getRoles().size());
            admin.getRoles().forEach(role -> {
                System.out.println("    - Role ID: " + role.getId() + ", Name: " + role.getName());
            });
        });
    }
}