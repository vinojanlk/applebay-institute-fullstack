package com.applebay.institute.dto.response;

import com.applebay.institute.entity.EnrollmentStatus;

import java.time.LocalDate;

public class EnrollmentResponse {

    private final Long id;
    private final Long studentId;
    private final String studentNumber;
    private final String firstName;
    private final String lastName;
    private final Long courseId;
    private final String courseCode;
    private final String courseName;
    private final Long batchId;
    private final String batchCode;
    private final String batchName;
    private final LocalDate enrolledOn;
    private final EnrollmentStatus status;

    public EnrollmentResponse(
            Long id,
            Long studentId,
            String studentNumber,
            String firstName,
            String lastName,
            Long courseId,
            String courseCode,
            String courseName,
            Long batchId,
            String batchCode,
            String batchName,
            LocalDate enrolledOn,
            EnrollmentStatus status
    ) {
        this.id = id;
        this.studentId = studentId;
        this.studentNumber = studentNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.batchId = batchId;
        this.batchCode = batchCode;
        this.batchName = batchName;
        this.enrolledOn = enrolledOn;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Long getCourseId() {
        return courseId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public Long getBatchId() {
        return batchId;
    }

    public String getBatchCode() {
        return batchCode;
    }

    public String getBatchName() {
        return batchName;
    }

    public LocalDate getEnrolledOn() {
        return enrolledOn;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }
}
