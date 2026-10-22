package com.applebay.institute.dto.response;

import java.math.BigDecimal;

public class AttendanceSummaryResponse {

    private final Long enrollmentId;
    private final long totalRecords;
    private final long attendedCount;
    private final BigDecimal attendancePercentage;
    private final boolean examEligible;

    public AttendanceSummaryResponse(
            Long enrollmentId,
            long totalRecords,
            long attendedCount,
            BigDecimal attendancePercentage,
            boolean examEligible
    ) {
        this.enrollmentId = enrollmentId;
        this.totalRecords = totalRecords;
        this.attendedCount = attendedCount;
        this.attendancePercentage = attendancePercentage;
        this.examEligible = examEligible;
    }

    public Long getEnrollmentId() {
        return enrollmentId;
    }

    public long getTotalRecords() {
        return totalRecords;
    }

    public long getAttendedCount() {
        return attendedCount;
    }

    public BigDecimal getAttendancePercentage() {
        return attendancePercentage;
    }

    public boolean isExamEligible() {
        return examEligible;
    }
}
