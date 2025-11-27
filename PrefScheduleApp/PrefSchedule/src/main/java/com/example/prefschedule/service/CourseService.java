package com.example.prefschedule.service;

import com.example.prefschedule.entity.Course;
import com.example.prefschedule.repository.CourseRepository;
import org.springframework.stereotype.Service;
import java.util.List;

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
}
