package com.example.prefschedule.entity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;
    private String code;
    private String abbr;
    private String name;
    private Integer groupCount;
    private String description;

    @ManyToOne
    @JoinColumn(name = "instructor_id")
    @JsonBackReference
    private Instructor instructor;

    @ManyToOne
    @JoinColumn(name = "pack_id")
    @JsonBackReference
    private Pack pack;

    public Course() {}

    public Course(String type, String code, String abbr, String name,
                  int groupCount, String description,
                  Instructor instructor, Pack pack) {
        this.type = type;
        this.code = code;
        this.abbr = abbr;
        this.name = name;
        this.groupCount = groupCount;
        this.description = description;
        this.instructor = instructor;
        this.pack = pack;
    }
    public boolean isCompulsory() {
        return "COMPULSORY".equalsIgnoreCase(this.type);
    }

    @Override
    public String toString() {
        return "Course{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", code='" + code + '\'' +
                ", abbr='" + abbr + '\'' +
                ", name='" + name + '\'' +
                ", groupCount=" + groupCount +
                ", description='" + description + '\'' +
                '}';
    }

}
