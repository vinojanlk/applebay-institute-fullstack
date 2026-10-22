package com.applebay.institute.service;

import com.applebay.institute.dto.request.PaymentRequest;
import com.applebay.institute.dto.response.FeeSummaryResponse;
import com.applebay.institute.dto.response.PaymentResponse;
import com.applebay.institute.entity.Enrollment;
import com.applebay.institute.entity.Payment;
import com.applebay.institute.exception.BusinessRuleException;
import com.applebay.institute.exception.ResourceNotFoundException;
import com.applebay.institute.repository.EnrollmentRepository;
import com.applebay.institute.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Service
public class PaymentService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final PaymentRepository paymentRepository;
    private final EnrollmentRepository enrollmentRepository;

    public PaymentService(
            PaymentRepository paymentRepository,
            EnrollmentRepository enrollmentRepository
    ) {
        this.paymentRepository = paymentRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        BigDecimal amount = request.getAmount();
        if (amount.compareTo(ZERO) <= 0) {
            throw new BusinessRuleException("Payment amount must be greater than zero");
        }

        Enrollment enrollment = findEnrollmentForUpdate(request.getEnrollmentId());
        BigDecimal totalPaid = getTotalPaid(request.getEnrollmentId());
        BigDecimal courseFee = enrollment.getCourse().getFee();
        BigDecimal outstandingBalance = courseFee.subtract(totalPaid);

        if (outstandingBalance.compareTo(ZERO) <= 0) {
            throw new BusinessRuleException("The enrollment has no outstanding balance");
        }
        if (amount.compareTo(outstandingBalance) > 0) {
            throw new BusinessRuleException("Payment amount cannot exceed the outstanding balance");
        }

        Payment payment = new Payment();
        payment.setEnrollment(enrollment);
        payment.setReceiptNumber(generateReceiptNumber());
        payment.setAmount(amount);
        payment.setPaidOn(request.getPaymentDate());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setReference(trimToNull(request.getReference()));
        return toResponse(paymentRepository.save(payment));
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentHistory(Long enrollmentId) {
        findEnrollment(enrollmentId);
        return paymentRepository.findAllByEnrollment_IdOrderByPaidOnAscIdAsc(enrollmentId)
                .stream()
                .map(PaymentService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public FeeSummaryResponse getFeeSummary(Long enrollmentId) {
        Enrollment enrollment = findEnrollment(enrollmentId);
        BigDecimal totalPaid = getTotalPaid(enrollmentId);
        BigDecimal courseFee = enrollment.getCourse().getFee();
        return new FeeSummaryResponse(
                enrollmentId,
                courseFee,
                totalPaid,
                courseFee.subtract(totalPaid)
        );
    }

    private Enrollment findEnrollmentForUpdate(Long enrollmentId) {
        return enrollmentRepository.findByIdForUpdate(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Enrollment " + enrollmentId + " was not found"
                ));
    }

    private Enrollment findEnrollment(Long enrollmentId) {
        return enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Enrollment " + enrollmentId + " was not found"
                ));
    }

    private BigDecimal getTotalPaid(Long enrollmentId) {
        BigDecimal totalPaid = paymentRepository.getTotalPaidByEnrollmentId(enrollmentId);
        return totalPaid == null ? ZERO : totalPaid;
    }

    private String generateReceiptNumber() {
        Long sequenceValue = paymentRepository.nextReceiptNumberValue();
        return "RCPT-" + String.format(Locale.ROOT, "%06d", sequenceValue);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }

    private static PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getEnrollment().getId(),
                payment.getReceiptNumber(),
                payment.getAmount(),
                payment.getPaidOn(),
                payment.getPaymentMethod(),
                payment.getReference()
        );
    }
}
