package com.example.prefschedule.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "student_preferences")
@Data
public class StudentPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    private int preferenceOrder;

    public StudentPreference() {}
    public StudentPreference(Student student, Course course, int preferenceOrder) {
        this.student = student;
        this.course = course;
        this.preferenceOrder = preferenceOrder;
    }
}
