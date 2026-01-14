package com.example.prefschedule.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "instructors")
@Data
public class Instructor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String name;
    private String email;

    @OneToMany(mappedBy = "instructor", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Course> courses = new ArrayList<>();

    public Instructor() {}

    public Instructor(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // Getters and Setters

    @Override
    public String toString() {
        return "Instructor{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
