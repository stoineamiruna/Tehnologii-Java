package com.example.prefschedule.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "instructor_course_preferences")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstructorCoursePreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    @ToString.Exclude
    @JsonBackReference
    private Course course;

    @Column(name = "compulsory_course_abbr", nullable = false)
    private String compulsoryCourseAbbr;

    @Column(name = "weight_percentage", nullable = false)
    private Double weightPercentage; // 0-100

    @Version
    private Long version;
}