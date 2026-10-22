package com.applebay.institute.controller;

import com.applebay.institute.dto.request.StudentEnrollmentPaymentRequest;
import com.applebay.institute.dto.response.StudentEnrollmentPaymentResponse;
import com.applebay.institute.service.StudentEnrollmentPaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student-enrollment-payments")
public class StudentEnrollmentPaymentController {

    private final StudentEnrollmentPaymentService workflowService;

    public StudentEnrollmentPaymentController(StudentEnrollmentPaymentService workflowService) {
        this.workflowService = workflowService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StudentEnrollmentPaymentResponse registerEnrollAndPay(
            @Valid @RequestBody StudentEnrollmentPaymentRequest request
    ) {
        return workflowService.registerEnrollAndPay(request);
    }
}
