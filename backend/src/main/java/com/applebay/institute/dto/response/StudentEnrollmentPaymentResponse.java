package com.applebay.institute.dto.response;

public class StudentEnrollmentPaymentResponse {

    private final StudentResponse student;
    private final EnrollmentResponse enrollment;
    private final PaymentResponse payment;

    public StudentEnrollmentPaymentResponse(
            StudentResponse student,
            EnrollmentResponse enrollment,
            PaymentResponse payment
    ) {
        this.student = student;
        this.enrollment = enrollment;
        this.payment = payment;
    }

    public StudentResponse getStudent() {
        return student;
    }

    public EnrollmentResponse getEnrollment() {
        return enrollment;
    }

    public PaymentResponse getPayment() {
        return payment;
    }
}
