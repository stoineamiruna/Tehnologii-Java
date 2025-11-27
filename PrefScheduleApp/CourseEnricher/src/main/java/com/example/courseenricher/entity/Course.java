package com.example.courseenricher.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;

@Entity
@Table(name = "courses")
@Data @Getter
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;
    private String name;

    @Getter
    @ManyToOne
    @JoinColumn(name = "pack_id")
    private Pack pack;

    public Pack getPack() {
        return pack;
    }

    public String getName() {
        return name;
    }
}