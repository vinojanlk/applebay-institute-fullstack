package com.applebay.institute.service;

import com.applebay.institute.dto.response.DashboardResponse;
import com.applebay.institute.entity.AttendanceStatus;
import com.applebay.institute.entity.CourseStatus;
import com.applebay.institute.repository.AttendanceRepository;
import com.applebay.institute.repository.CourseRepository;
import com.applebay.institute.repository.PaymentRepository;
import com.applebay.institute.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class DashboardService {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final AttendanceRepository attendanceRepository;
    private final PaymentRepository paymentRepository;

    public DashboardService(
            StudentRepository studentRepository,
            CourseRepository courseRepository,
            AttendanceRepository attendanceRepository,
            PaymentRepository paymentRepository
    ) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.attendanceRepository = attendanceRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getSummary() {
        long totalStudents = studentRepository.count();
        long activeCourses = courseRepository.countByStatus(CourseStatus.ACTIVE);
        long todayPresentCount = attendanceRepository.countByAttendanceDateAndStatusIn(
                LocalDate.now(),
                List.of(AttendanceStatus.PRESENT, AttendanceStatus.LATE)
        );
        BigDecimal totalFeesCollected = paymentRepository.getTotalFeesCollected();
        if (totalFeesCollected == null) {
            totalFeesCollected = BigDecimal.ZERO;
        }

        return new DashboardResponse(
                totalStudents,
                activeCourses,
                todayPresentCount,
                totalFeesCollected
        );
    }
}
