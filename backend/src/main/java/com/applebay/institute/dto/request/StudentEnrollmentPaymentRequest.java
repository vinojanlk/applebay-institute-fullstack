package com.applebay.institute.dto.request;

import com.applebay.institute.entity.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class StudentEnrollmentPaymentRequest {

    @Valid
    @NotNull
    private StudentRegistrationRequest student;

    @NotNull
    @Positive
    private Long courseId;

    @NotNull
    @Positive
    private Long batchId;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal amount;

    @NotNull
    private LocalDate paymentDate;

    @NotNull
    private PaymentMethod paymentMethod;

    @Size(max = 100)
    private String reference;

    public StudentRegistrationRequest getStudent() {
        return student;
    }

    public void setStudent(StudentRegistrationRequest student) {
        this.student = student;
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
}
