package com.applebay.institute.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class EnrollmentRequest {

    @NotNull
    @Positive
    private Long studentId;

    @NotNull
    @Positive
    private Long courseId;

    @NotNull
    @Positive
    private Long batchId;

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }
}
