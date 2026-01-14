package com.example.prefschedule.service;

import com.example.prefschedule.entity.Course;
import com.example.prefschedule.repository.CourseRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public List<Course> getAll() {
        return courseRepository.findAll();
    }

    public Course save(Course course) {
        return courseRepository.save(course);
    }

    public void incrementGroup(Long id) {
        courseRepository.incrementGroupCount(id);
    }


    public Optional<Course> getById(Long id) {
        return courseRepository.findById(id);
    }

    public void deleteById(Long id) {
        courseRepository.deleteById(id);
    }

    public Optional<Course> findByCode(String code) {
        return courseRepository.findByCode(code);
    }
    public List<Course> getCoursesByInstructorId(Long instructorId) {
        return courseRepository.findByInstructorId(instructorId);
    }
    public List<Course> searchByName(String name) {
        return courseRepository.findByNameContainingIgnoreCase(name);
    }
}
