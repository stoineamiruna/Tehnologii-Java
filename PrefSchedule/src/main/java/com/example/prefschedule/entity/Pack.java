package com.example.prefschedule.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "packs")
public class Pack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int year;
    private int semester;
    private String name;

    @OneToMany(mappedBy = "pack", cascade = CascadeType.ALL)
    private List<Course> courses = new ArrayList<>();

    public Pack() {}

    public Pack(int year, int semester, String name) {
        this.year = year;
        this.semester = semester;
        this.name = name;
    }

    @Override
    public String toString() {
        return "Pack{" +
                "id=" + id +
                ", year=" + year +
                ", semester=" + semester +
                ", name='" + name + '\'' +
                '}';
    }
}
