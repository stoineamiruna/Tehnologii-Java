package com.example.prefschedule.service;

import com.example.prefschedule.entity.Student;
import com.example.prefschedule.repository.StudentRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public List<Student> getAll() {
        return studentRepository.findAll();
    }

    public Student save(Student student) {
        return studentRepository.save(student);
    }

    public void updateEmail(Long id, String email) {
        studentRepository.updateEmail(id, email);
    }
}
