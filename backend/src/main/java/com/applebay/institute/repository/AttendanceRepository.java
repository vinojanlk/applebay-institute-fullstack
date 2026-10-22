package com.applebay.institute.repository;

import com.applebay.institute.entity.Attendance;
import com.applebay.institute.entity.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    long countByAttendanceDateAndStatusIn(
            LocalDate attendanceDate,
            Collection<AttendanceStatus> statuses
    );

    List<Attendance> findAllByEnrollment_Batch_IdAndAttendanceDate(Long batchId, LocalDate attendanceDate);

    Optional<Attendance> findByEnrollment_IdAndAttendanceDate(Long enrollmentId, LocalDate attendanceDate);

    List<Attendance> findAllByEnrollment_IdOrderByAttendanceDateAsc(Long enrollmentId);
}
