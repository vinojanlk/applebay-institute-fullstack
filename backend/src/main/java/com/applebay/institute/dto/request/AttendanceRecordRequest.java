package com.applebay.institute.dto.request;

import com.applebay.institute.entity.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class AttendanceRecordRequest {

    @NotNull
    @Positive
    private Long enrollmentId;

    @NotNull
    private AttendanceStatus status;

    @Size(max = 500)
    private String remarks;

    public Long getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(Long enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
