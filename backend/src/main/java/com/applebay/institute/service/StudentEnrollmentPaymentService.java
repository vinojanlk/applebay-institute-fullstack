package com.applebay.institute.service;

import com.applebay.institute.dto.request.EnrollmentRequest;
import com.applebay.institute.dto.request.PaymentRequest;
import com.applebay.institute.dto.request.StudentEnrollmentPaymentRequest;
import com.applebay.institute.dto.response.EnrollmentResponse;
import com.applebay.institute.dto.response.PaymentResponse;
import com.applebay.institute.dto.response.StudentEnrollmentPaymentResponse;
import com.applebay.institute.dto.response.StudentResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentEnrollmentPaymentService {

    private final StudentService studentService;
    private final EnrollmentService enrollmentService;
    private final PaymentService paymentService;

    public StudentEnrollmentPaymentService(
            StudentService studentService,
            EnrollmentService enrollmentService,
            PaymentService paymentService
    ) {
        this.studentService = studentService;
        this.enrollmentService = enrollmentService;
        this.paymentService = paymentService;
    }

    @Transactional
    public StudentEnrollmentPaymentResponse registerEnrollAndPay(
            StudentEnrollmentPaymentRequest request
    ) {
        StudentResponse student = studentService.registerStudent(request.getStudent());

        EnrollmentRequest enrollmentRequest = new EnrollmentRequest();
        enrollmentRequest.setStudentId(student.getId());
        enrollmentRequest.setCourseId(request.getCourseId());
        enrollmentRequest.setBatchId(request.getBatchId());
        EnrollmentResponse enrollment = enrollmentService.createEnrollment(enrollmentRequest);

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setEnrollmentId(enrollment.getId());
        paymentRequest.setAmount(request.getAmount());
        paymentRequest.setPaymentDate(request.getPaymentDate());
        paymentRequest.setPaymentMethod(request.getPaymentMethod());
        paymentRequest.setReference(request.getReference());
        PaymentResponse payment = paymentService.createPayment(paymentRequest);

        // PostgreSQL sequences are not rolled back, so failed workflows may leave number gaps.
        return new StudentEnrollmentPaymentResponse(student, enrollment, payment);
    }
}
