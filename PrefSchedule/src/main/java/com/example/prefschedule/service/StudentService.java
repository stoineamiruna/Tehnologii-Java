package com.example.prefschedule.service;

import com.example.prefschedule.entity.Student;
import com.example.prefschedule.repository.StudentRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
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

    public Optional<Student> getById(Long id) {
        return studentRepository.findById(id);
    }

    public void deleteById(Long id) {
        studentRepository.deleteById(id);
    }
    public void updateEmail(Long id, String email) {
        studentRepository.updateEmail(id, email);
    }
    public Student updateStudent(Long id, Student updatedStudent) {
        Student existing = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        existing.setCode(updatedStudent.getCode());
        existing.setName(updatedStudent.getName());
        existing.setEmail(updatedStudent.getEmail());
        existing.setYear(updatedStudent.getYear());

        return studentRepository.save(existing);
    }

}
