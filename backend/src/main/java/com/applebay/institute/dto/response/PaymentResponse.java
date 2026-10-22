package com.applebay.institute.dto.response;

import com.applebay.institute.entity.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PaymentResponse {

    private final Long id;
    private final Long enrollmentId;
    private final String receiptNumber;
    private final BigDecimal amount;
    private final LocalDate paymentDate;
    private final PaymentMethod paymentMethod;
    private final String reference;

    public PaymentResponse(
            Long id,
            Long enrollmentId,
            String receiptNumber,
            BigDecimal amount,
            LocalDate paymentDate,
            PaymentMethod paymentMethod,
            String reference
    ) {
        this.id = id;
        this.enrollmentId = enrollmentId;
        this.receiptNumber = receiptNumber;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
        this.reference = reference;
    }

    public Long getId() {
        return id;
    }

    public Long getEnrollmentId() {
        return enrollmentId;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public String getReference() {
        return reference;
    }
}
