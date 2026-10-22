package com.applebay.institute.service;

import com.applebay.institute.dto.response.DashboardResponse;
import com.applebay.institute.entity.AttendanceStatus;
import com.applebay.institute.entity.CourseStatus;
import com.applebay.institute.repository.AttendanceRepository;
import com.applebay.institute.repository.CourseRepository;
import com.applebay.institute.repository.PaymentRepository;
import com.applebay.institute.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void combinesNonZeroSummaryValues() {
        when(studentRepository.count()).thenReturn(24L);
        when(courseRepository.countByStatus(CourseStatus.ACTIVE)).thenReturn(5L);
        when(attendanceRepository.countByAttendanceDateAndStatusIn(
                eq(LocalDate.now()),
                eq(List.of(AttendanceStatus.PRESENT, AttendanceStatus.LATE))
        )).thenReturn(9L);
        when(paymentRepository.getTotalFeesCollected()).thenReturn(new BigDecimal("12500.75"));

        DashboardResponse response = dashboardService.getSummary();

        assertEquals(24L, response.getTotalStudents());
        assertEquals(5L, response.getActiveCourses());
        assertEquals(9L, response.getTodayPresentCount());
        assertEquals(new BigDecimal("12500.75"), response.getTotalFeesCollected());
    }

    @Test
    void returnsZeroForStudentsWhenRepositoryHasNone() {
        when(studentRepository.count()).thenReturn(0L);
        when(paymentRepository.getTotalFeesCollected()).thenReturn(BigDecimal.ZERO);

        DashboardResponse response = dashboardService.getSummary();

        assertEquals(0L, response.getTotalStudents());
    }

    @Test
    void returnsZeroForActiveCoursesWhenRepositoryHasNone() {
        when(courseRepository.countByStatus(CourseStatus.ACTIVE)).thenReturn(0L);
        when(paymentRepository.getTotalFeesCollected()).thenReturn(BigDecimal.ZERO);

        DashboardResponse response = dashboardService.getSummary();

        assertEquals(0L, response.getActiveCourses());
    }

    @Test
    void returnsZeroWhenThereIsNoAttendanceToday() {
        when(attendanceRepository.countByAttendanceDateAndStatusIn(
                eq(LocalDate.now()),
                eq(List.of(AttendanceStatus.PRESENT, AttendanceStatus.LATE))
        )).thenReturn(0L);
        when(paymentRepository.getTotalFeesCollected()).thenReturn(BigDecimal.ZERO);

        DashboardResponse response = dashboardService.getSummary();

        assertEquals(0L, response.getTodayPresentCount());
    }

    @Test
    void countsPresentAndLateStatusesForTodayButNotAbsent() {
        when(attendanceRepository.countByAttendanceDateAndStatusIn(
                eq(LocalDate.now()),
                eq(List.of(AttendanceStatus.PRESENT, AttendanceStatus.LATE))
        )).thenReturn(3L);
        when(paymentRepository.getTotalFeesCollected()).thenReturn(BigDecimal.ZERO);

        dashboardService.getSummary();

        verify(attendanceRepository).countByAttendanceDateAndStatusIn(
                eq(LocalDate.now()),
                eq(List.of(AttendanceStatus.PRESENT, AttendanceStatus.LATE))
        );
    }

    @Test
    void noPaymentsReturnsBigDecimalZero() {
        when(paymentRepository.getTotalFeesCollected()).thenReturn(BigDecimal.ZERO);

        DashboardResponse response = dashboardService.getSummary();

        assertEquals(BigDecimal.ZERO, response.getTotalFeesCollected());
    }

    @Test
    void nullPaymentTotalIsConvertedToBigDecimalZero() {
        when(paymentRepository.getTotalFeesCollected()).thenReturn(null);

        DashboardResponse response = dashboardService.getSummary();

        assertEquals(BigDecimal.ZERO, response.getTotalFeesCollected());
    }
}
