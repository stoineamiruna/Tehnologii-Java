package com.example.enricher.dto;

public class GradeEvent {
    private String studentCode;
    private String courseCode;
    private double grade;

    public GradeEvent() {}

    public GradeEvent(String studentCode, String courseCode, double grade) {
        this.studentCode = studentCode;
        this.courseCode = courseCode;
        this.grade = grade;
    }
    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public double getGrade() { return grade; }
    public void setGrade(double grade) { this.grade = grade; }

    @Override
    public String toString() {
        return "GradeEvent{studentCode='" + studentCode + "', courseCode='" + courseCode + "', grade=" + grade + "}";
    }
}