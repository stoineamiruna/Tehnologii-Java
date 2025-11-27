package com.example.courseenricher.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;

@Entity
@Table(name = "packs")
@Data @Getter
public class Pack {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer year;
    private String semester;
    private String name;

    public String getSemester() {
        return semester;
    }
}