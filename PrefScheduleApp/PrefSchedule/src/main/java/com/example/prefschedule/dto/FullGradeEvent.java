package com.example.prefschedule.dto;

public class FullGradeEvent {
    private String studentCode;
    private String studentName;
    private Integer year;
    private String courseCode;
    private String courseName;
    private String semester;
    private double grade;
    public FullGradeEvent() {
    }
    public FullGradeEvent(String studentCode, String studentName, Integer year,
                          String courseCode, String courseName, String semester, double grade) {
        this.studentCode = studentCode;
        this.studentName = studentName;
        this.year = year;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.semester = semester;
        this.grade = grade;
    }
    public String getStudentCode() {
        return studentCode;
    }

    public String getStudentName() {
        return studentName;
    }

    public Integer getYear() {
        return year;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getSemester() {
        return semester;
    }

    public double getGrade() {
        return grade;
    }
    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public void setGrade(double grade) {
        this.grade = grade;
    }

    @Override
    public String toString() {
        return "FullGradeEvent{" +
                "studentCode='" + studentCode + '\'' +
                ", studentName='" + studentName + '\'' +
                ", year=" + year +
                ", courseCode='" + courseCode + '\'' +
                ", courseName='" + courseName + '\'' +
                ", semester='" + semester + '\'' +
                ", grade=" + grade +
                '}';
    }
}