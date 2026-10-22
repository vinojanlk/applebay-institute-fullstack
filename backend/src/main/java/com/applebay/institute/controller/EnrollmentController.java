package com.applebay.institute.controller;

import com.applebay.institute.dto.request.EnrollmentRequest;
import com.applebay.institute.dto.response.EnrollmentResponse;
import com.applebay.institute.service.EnrollmentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping("/enrollments")
    @ResponseStatus(HttpStatus.CREATED)
    public EnrollmentResponse createEnrollment(
            @Valid @RequestBody EnrollmentRequest request
    ) {
        return enrollmentService.createEnrollment(request);
    }

    @GetMapping("/enrollments/{id}")
    public EnrollmentResponse getEnrollment(@PathVariable @Min(1) Long id) {
        return enrollmentService.getEnrollment(id);
    }

    @GetMapping("/students/{studentId}/enrollments")
    public List<EnrollmentResponse> getStudentEnrollments(
            @PathVariable @Min(1) Long studentId
    ) {
        return enrollmentService.getStudentEnrollments(studentId);
    }
}
