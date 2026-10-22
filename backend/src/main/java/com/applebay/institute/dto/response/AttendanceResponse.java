package com.applebay.institute.dto.response;

import com.applebay.institute.entity.AttendanceStatus;

public class AttendanceResponse {

    private final Long enrollmentId;
    private final Long studentId;
    private final String studentNumber;
    private final String firstName;
    private final String lastName;
    private final AttendanceStatus status;
    private final String remarks;

    public AttendanceResponse(
            Long enrollmentId,
            Long studentId,
            String studentNumber,
            String firstName,
            String lastName,
            AttendanceStatus status,
            String remarks
    ) {
        this.enrollmentId = enrollmentId;
        this.studentId = studentId;
        this.studentNumber = studentNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.status = status;
        this.remarks = remarks;
    }

    public Long getEnrollmentId() {
        return enrollmentId;
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

    public AttendanceStatus getStatus() {
        return status;
    }

    public String getRemarks() {
        return remarks;
    }
}
