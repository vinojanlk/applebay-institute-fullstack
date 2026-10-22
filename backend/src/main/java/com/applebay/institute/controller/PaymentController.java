package com.applebay.institute.controller;

import com.applebay.institute.dto.request.PaymentRequest;
import com.applebay.institute.dto.response.FeeSummaryResponse;
import com.applebay.institute.dto.response.PaymentResponse;
import com.applebay.institute.service.PaymentService;
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
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/payments")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse createPayment(@Valid @RequestBody PaymentRequest request) {
        return paymentService.createPayment(request);
    }

    @GetMapping("/enrollments/{enrollmentId}/payments")
    public List<PaymentResponse> getPaymentHistory(
            @PathVariable @Min(1) Long enrollmentId
    ) {
        return paymentService.getPaymentHistory(enrollmentId);
    }

    @GetMapping("/enrollments/{enrollmentId}/fee-summary")
    public FeeSummaryResponse getFeeSummary(
            @PathVariable @Min(1) Long enrollmentId
    ) {
        return paymentService.getFeeSummary(enrollmentId);
    }
}
