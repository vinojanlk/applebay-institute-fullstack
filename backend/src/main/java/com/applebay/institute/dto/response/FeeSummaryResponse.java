package com.applebay.institute.dto.response;

import java.math.BigDecimal;

public class FeeSummaryResponse {

    private final Long enrollmentId;
    private final BigDecimal courseFee;
    private final BigDecimal totalPaid;
    private final BigDecimal outstandingBalance;

    public FeeSummaryResponse(
            Long enrollmentId,
            BigDecimal courseFee,
            BigDecimal totalPaid,
            BigDecimal outstandingBalance
    ) {
        this.enrollmentId = enrollmentId;
        this.courseFee = courseFee;
        this.totalPaid = totalPaid;
        this.outstandingBalance = outstandingBalance;
    }

    public Long getEnrollmentId() {
        return enrollmentId;
    }

    public BigDecimal getCourseFee() {
        return courseFee;
    }

    public BigDecimal getTotalPaid() {
        return totalPaid;
    }

    public BigDecimal getOutstandingBalance() {
        return outstandingBalance;
    }
}
