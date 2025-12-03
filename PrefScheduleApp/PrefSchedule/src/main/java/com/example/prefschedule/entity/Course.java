package com.example.prefschedule.entity;
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
    private Instructor instructor;

    @ManyToOne
    @JoinColumn(name = "pack_id")
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
