package com.example.prefschedule.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class StudentGrade {
    @Id @GeneratedValue
    private Long id;

    private String studentCode;
    private String courseCode;
    private double grade;
}

