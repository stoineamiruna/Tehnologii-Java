package com.example.enricher.dto;

public class EnrichedGradeEvent {
    private String studentCode;
    private String studentName;
    private Integer year;
    private String courseCode;
    private double grade;

    public EnrichedGradeEvent() {}

    public EnrichedGradeEvent(String studentCode, String studentName, Integer year, String courseCode, double grade) {
        this.studentCode = studentCode;
        this.studentName = studentName;
        this.year = year;
        this.courseCode = courseCode;
        this.grade = grade;
    }
    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public double getGrade() { return grade; }
    public void setGrade(double grade) { this.grade = grade; }

    @Override
    public String toString() {
        return "EnrichedGradeEvent{studentCode='" + studentCode + "', studentName='" + studentName +
                "', year=" + year + ", courseCode='" + courseCode + "', grade=" + grade + "}";
    }
}