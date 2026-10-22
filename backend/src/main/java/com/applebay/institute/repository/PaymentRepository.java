package com.applebay.institute.repository;

import com.applebay.institute.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findAllByEnrollment_IdOrderByPaidOnAscIdAsc(Long enrollmentId);

    @Query("SELECT COALESCE(SUM(payment.amount), 0) FROM Payment payment "
            + "WHERE payment.enrollment.id = :enrollmentId")
    BigDecimal getTotalPaidByEnrollmentId(@Param("enrollmentId") Long enrollmentId);

    @Query("SELECT COALESCE(SUM(payment.amount), 0) FROM Payment payment")
    BigDecimal getTotalFeesCollected();

    @Query(value = "SELECT nextval('payment_receipt_seq')", nativeQuery = true)
    Long nextReceiptNumberValue();
}
