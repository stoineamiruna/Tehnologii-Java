package com.example.prefschedule.runner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.prefschedule.entity.Student;
import com.example.prefschedule.entity.Instructor;
import com.example.prefschedule.entity.Pack;
import com.example.prefschedule.entity.Course;
import com.example.prefschedule.repository.StudentRepository;
import com.example.prefschedule.repository.InstructorRepository;
import com.example.prefschedule.repository.PackRepository;
import com.example.prefschedule.repository.CourseRepository;
import com.github.javafaker.Faker;

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
    }
}
