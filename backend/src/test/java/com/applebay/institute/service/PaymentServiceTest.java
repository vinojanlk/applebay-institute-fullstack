package com.applebay.institute.service;

import com.applebay.institute.dto.request.PaymentRequest;
import com.applebay.institute.dto.response.FeeSummaryResponse;
import com.applebay.institute.dto.response.PaymentResponse;
import com.applebay.institute.entity.Course;
import com.applebay.institute.entity.Enrollment;
import com.applebay.institute.entity.Payment;
import com.applebay.institute.entity.PaymentMethod;
import com.applebay.institute.exception.BusinessRuleException;
import com.applebay.institute.exception.ResourceNotFoundException;
import com.applebay.institute.repository.EnrollmentRepository;
import com.applebay.institute.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void createsPaymentAndGeneratesReceiptNumber() {
        Enrollment enrollment = enrollment();
        when(enrollmentRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(enrollment));
        when(paymentRepository.getTotalPaidByEnrollmentId(5L)).thenReturn(new BigDecimal("100.00"));
        when(paymentRepository.nextReceiptNumberValue()).thenReturn(1L);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(12L);
            return payment;
        });

        PaymentResponse response = paymentService.createPayment(request("200.00"));

        assertEquals(12L, response.getId());
        assertEquals(5L, response.getEnrollmentId());
        assertEquals("RCPT-000001", response.getReceiptNumber());
        assertEquals(new BigDecimal("200.00"), response.getAmount());
        assertEquals(LocalDate.of(2026, 10, 19), response.getPaymentDate());
        assertEquals(PaymentMethod.CASH, response.getPaymentMethod());
        verify(paymentRepository).nextReceiptNumberValue();
    }

    @Test
    void missingEnrollmentIsRejected() {
        when(enrollmentRepository.findByIdForUpdate(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> paymentService.createPayment(request("10.00")));
        verify(paymentRepository, never()).getTotalPaidByEnrollmentId(5L);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void zeroAmountIsRejected() {
        assertThrows(BusinessRuleException.class,
                () -> paymentService.createPayment(request("0.00")));
        verify(enrollmentRepository, never()).findByIdForUpdate(5L);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void negativeAmountIsRejected() {
        assertThrows(BusinessRuleException.class,
                () -> paymentService.createPayment(request("-1.00")));
        verify(enrollmentRepository, never()).findByIdForUpdate(5L);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void overpaymentIsRejected() {
        when(enrollmentRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(enrollment()));
        when(paymentRepository.getTotalPaidByEnrollmentId(5L)).thenReturn(new BigDecimal("100.00"));

        assertThrows(BusinessRuleException.class,
                () -> paymentService.createPayment(request("400.01")));
        verify(paymentRepository, never()).nextReceiptNumberValue();
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void paymentEqualToOutstandingBalanceIsAccepted() {
        when(enrollmentRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(enrollment()));
        when(paymentRepository.getTotalPaidByEnrollmentId(5L)).thenReturn(new BigDecimal("100.00"));
        when(paymentRepository.nextReceiptNumberValue()).thenReturn(2L);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = paymentService.createPayment(request("400.00"));

        assertEquals(new BigDecimal("400.00"), response.getAmount());
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void nullTotalFromRepositoryIsTreatedAsZero() {
        when(enrollmentRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(enrollment()));
        when(paymentRepository.getTotalPaidByEnrollmentId(5L)).thenReturn(null);
        when(paymentRepository.nextReceiptNumberValue()).thenReturn(3L);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        paymentService.createPayment(request("10.00"));

        verify(paymentRepository).getTotalPaidByEnrollmentId(5L);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void totalPaidCalculationIsReflectedInFeeSummary() {
        when(enrollmentRepository.findById(5L)).thenReturn(Optional.of(enrollment()));
        when(paymentRepository.getTotalPaidByEnrollmentId(5L)).thenReturn(new BigDecimal("150.25"));

        FeeSummaryResponse summary = paymentService.getFeeSummary(5L);

        assertEquals(new BigDecimal("500.00"), summary.getCourseFee());
        assertEquals(new BigDecimal("150.25"), summary.getTotalPaid());
        assertEquals(new BigDecimal("349.75"), summary.getOutstandingBalance());
    }

    @Test
    void noPreviousPaymentsProducesZeroTotalPaid() {
        when(enrollmentRepository.findById(5L)).thenReturn(Optional.of(enrollment()));
        when(paymentRepository.getTotalPaidByEnrollmentId(5L)).thenReturn(BigDecimal.ZERO);

        FeeSummaryResponse summary = paymentService.getFeeSummary(5L);

        assertEquals(BigDecimal.ZERO, summary.getTotalPaid());
        assertEquals(new BigDecimal("500.00"), summary.getOutstandingBalance());
    }

    @Test
    void zeroOutstandingBalanceRejectsAnotherPayment() {
        when(enrollmentRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(enrollment()));
        when(paymentRepository.getTotalPaidByEnrollmentId(5L)).thenReturn(new BigDecimal("500.00"));

        assertThrows(BusinessRuleException.class,
                () -> paymentService.createPayment(request("1.00")));
        verify(paymentRepository, never()).nextReceiptNumberValue();
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void paymentHistoryMapsRepositoryOrderToResponseDtos() {
        Payment earlier = payment(10L, "RCPT-000010", "20.00", LocalDate.of(2026, 10, 1));
        Payment later = payment(11L, "RCPT-000011", "30.00", LocalDate.of(2026, 10, 2));
        when(enrollmentRepository.findById(5L)).thenReturn(Optional.of(enrollment()));
        when(paymentRepository.findAllByEnrollment_IdOrderByPaidOnAscIdAsc(5L))
                .thenReturn(List.of(earlier, later));

        List<PaymentResponse> history = paymentService.getPaymentHistory(5L);

        assertEquals(2, history.size());
        assertEquals("RCPT-000010", history.get(0).getReceiptNumber());
        assertEquals("RCPT-000011", history.get(1).getReceiptNumber());
        assertEquals(new BigDecimal("20.00"), history.get(0).getAmount());
        verify(enrollmentRepository, never()).findByIdForUpdate(5L);
    }

    @Test
    void feeSummaryReturnsCorrectCourseFeePaidAndBalance() {
        when(enrollmentRepository.findById(5L)).thenReturn(Optional.of(enrollment()));
        when(paymentRepository.getTotalPaidByEnrollmentId(5L)).thenReturn(new BigDecimal("125.50"));

        FeeSummaryResponse summary = paymentService.getFeeSummary(5L);

        assertEquals(5L, summary.getEnrollmentId());
        assertEquals(new BigDecimal("500.00"), summary.getCourseFee());
        assertEquals(new BigDecimal("125.50"), summary.getTotalPaid());
        assertEquals(new BigDecimal("374.50"), summary.getOutstandingBalance());
        verify(enrollmentRepository, never()).findByIdForUpdate(5L);
    }

    @Test
    void paymentCreationLocksEnrollmentBeforeCalculatingBalance() {
        when(enrollmentRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(enrollment()));
        when(paymentRepository.getTotalPaidByEnrollmentId(5L)).thenReturn(new BigDecimal("100.00"));
        when(paymentRepository.nextReceiptNumberValue()).thenReturn(4L);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        paymentService.createPayment(request("10.00"));

        InOrder order = inOrder(enrollmentRepository, paymentRepository);
        order.verify(enrollmentRepository).findByIdForUpdate(5L);
        order.verify(paymentRepository).getTotalPaidByEnrollmentId(5L);
    }

    private PaymentRequest request(String amount) {
        PaymentRequest request = new PaymentRequest();
        request.setEnrollmentId(5L);
        request.setAmount(new BigDecimal(amount));
        request.setPaymentDate(LocalDate.of(2026, 10, 19));
        request.setPaymentMethod(PaymentMethod.CASH);
        request.setReference(" cash desk ");
        return request;
    }

    private Enrollment enrollment() {
        Course course = new Course();
        course.setId(2L);
        course.setFee(new BigDecimal("500.00"));

        Enrollment enrollment = new Enrollment();
        enrollment.setId(5L);
        enrollment.setCourse(course);
        return enrollment;
    }

    private Payment payment(Long id, String receiptNumber, String amount, LocalDate paidOn) {
        Payment payment = new Payment();
        payment.setId(id);
        payment.setEnrollment(enrollment());
        payment.setReceiptNumber(receiptNumber);
        payment.setAmount(new BigDecimal(amount));
        payment.setPaidOn(paidOn);
        payment.setPaymentMethod(PaymentMethod.CASH);
        return payment;
    }
}
