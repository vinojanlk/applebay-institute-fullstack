package com.applebay.institute.service;

import com.applebay.institute.dto.request.AttendanceRecordRequest;
import com.applebay.institute.dto.request.ClassAttendanceRequest;
import com.applebay.institute.dto.response.AttendanceResponse;
import com.applebay.institute.dto.response.AttendanceSummaryResponse;
import com.applebay.institute.dto.response.ClassAttendanceResponse;
import com.applebay.institute.entity.Attendance;
import com.applebay.institute.entity.AttendanceStatus;
import com.applebay.institute.entity.Batch;
import com.applebay.institute.entity.Enrollment;
import com.applebay.institute.entity.EnrollmentStatus;
import com.applebay.institute.entity.Student;
import com.applebay.institute.exception.BusinessRuleException;
import com.applebay.institute.exception.ResourceNotFoundException;
import com.applebay.institute.repository.AttendanceRepository;
import com.applebay.institute.repository.BatchRepository;
import com.applebay.institute.repository.EnrollmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class AttendanceService {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal ELIGIBILITY_PERCENTAGE = BigDecimal.valueOf(80);

    private final AttendanceRepository attendanceRepository;
    private final BatchRepository batchRepository;
    private final EnrollmentRepository enrollmentRepository;

    public AttendanceService(
            AttendanceRepository attendanceRepository,
            BatchRepository batchRepository,
            EnrollmentRepository enrollmentRepository
    ) {
        this.attendanceRepository = attendanceRepository;
        this.batchRepository = batchRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Transactional(readOnly = true)
    public ClassAttendanceResponse getClassAttendance(Long batchId, LocalDate date) {
        Batch batch = findBatch(batchId);
        List<Enrollment> activeEnrollments = findActiveEnrollments(batchId);
        List<Attendance> existingRecords =
                attendanceRepository.findAllByEnrollment_Batch_IdAndAttendanceDate(batchId, date);
        Map<Long, Attendance> attendanceByEnrollment = new HashMap<>();
        for (Attendance attendance : existingRecords) {
            attendanceByEnrollment.put(attendance.getEnrollment().getId(), attendance);
        }

        List<AttendanceResponse> records = new ArrayList<>();
        for (Enrollment enrollment : activeEnrollments) {
            Attendance attendance = attendanceByEnrollment.get(enrollment.getId());
            records.add(toAttendanceResponse(enrollment, attendance));
        }
        return toClassResponse(batch, date, records);
    }

    @Transactional
    public ClassAttendanceResponse submitClassAttendance(ClassAttendanceRequest request) {
        Batch batch = findBatch(request.getBatchId());
        List<Enrollment> activeEnrollments = findActiveEnrollments(batch.getId());
        Map<Long, Enrollment> activeEnrollmentById = new HashMap<>();
        for (Enrollment enrollment : activeEnrollments) {
            activeEnrollmentById.put(enrollment.getId(), enrollment);
        }

        Set<Long> submittedEnrollmentIds = new HashSet<>();
        for (AttendanceRecordRequest record : request.getRecords()) {
            if (record == null || record.getEnrollmentId() == null || record.getStatus() == null) {
                throw new BusinessRuleException(
                        "Each attendance record must include an enrollment ID and status"
                );
            }
            if (!submittedEnrollmentIds.add(record.getEnrollmentId())) {
                throw new BusinessRuleException(
                        "An enrollment can appear only once in a class attendance request"
                );
            }
            if (!activeEnrollmentById.containsKey(record.getEnrollmentId())) {
                throw new BusinessRuleException(
                        "Every attendance record must belong to an active enrollment in the selected batch"
                );
            }
        }

        if (!submittedEnrollmentIds.equals(activeEnrollmentById.keySet())) {
            throw new BusinessRuleException(
                    "Attendance must include every active enrollment in the selected batch"
            );
        }

        List<Attendance> existingRecords =
                attendanceRepository.findAllByEnrollment_Batch_IdAndAttendanceDate(
                        batch.getId(),
                        request.getDate()
                );
        Map<Long, Attendance> existingByEnrollment = new HashMap<>();
        for (Attendance attendance : existingRecords) {
            existingByEnrollment.put(attendance.getEnrollment().getId(), attendance);
        }

        List<Attendance> recordsToSave = new ArrayList<>();
        for (AttendanceRecordRequest recordRequest : request.getRecords()) {
            Enrollment enrollment = activeEnrollmentById.get(recordRequest.getEnrollmentId());
            Attendance attendance = existingByEnrollment.get(recordRequest.getEnrollmentId());
            if (attendance == null) {
                attendance = new Attendance();
                attendance.setEnrollment(enrollment);
                attendance.setAttendanceDate(request.getDate());
            }
            attendance.setStatus(recordRequest.getStatus());
            attendance.setRemarks(trimToNull(recordRequest.getRemarks()));
            recordsToSave.add(attendance);
        }

        List<Attendance> savedRecords = attendanceRepository.saveAll(recordsToSave);
        List<AttendanceResponse> responses = new ArrayList<>();
        for (Attendance attendance : savedRecords) {
            responses.add(toAttendanceResponse(attendance.getEnrollment(), attendance));
        }
        return toClassResponse(batch, request.getDate(), responses);
    }

    @Transactional(readOnly = true)
    public AttendanceSummaryResponse getStudentSummary(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Enrollment " + enrollmentId + " was not found"
                ));
        List<Attendance> records =
                attendanceRepository.findAllByEnrollment_IdOrderByAttendanceDateAsc(enrollmentId);

        long attendedCount = records.stream()
                .filter(record -> isAttended(record.getStatus()))
                .count();
        long totalRecords = records.size();
        BigDecimal percentage = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        if (totalRecords > 0) {
            percentage = BigDecimal.valueOf(attendedCount)
                    .multiply(ONE_HUNDRED)
                    .divide(BigDecimal.valueOf(totalRecords), 2, RoundingMode.HALF_UP);
        }

        return new AttendanceSummaryResponse(
                enrollment.getId(),
                totalRecords,
                attendedCount,
                percentage,
                percentage.compareTo(ELIGIBILITY_PERCENTAGE) >= 0
        );
    }

    private Batch findBatch(Long batchId) {
        return batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch " + batchId + " was not found"));
    }

    private List<Enrollment> findActiveEnrollments(Long batchId) {
        return enrollmentRepository
                .findAllByBatch_IdAndStatusOrderByStudent_LastNameAscStudent_FirstNameAsc(
                        batchId,
                        EnrollmentStatus.ACTIVE
                );
    }

    private static boolean isAttended(AttendanceStatus status) {
        return status == AttendanceStatus.PRESENT || status == AttendanceStatus.LATE;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }

    private static AttendanceResponse toAttendanceResponse(
            Enrollment enrollment,
            Attendance attendance
    ) {
        Student student = enrollment.getStudent();
        return new AttendanceResponse(
                enrollment.getId(),
                student.getId(),
                student.getStudentNumber(),
                student.getFirstName(),
                student.getLastName(),
                attendance == null ? null : attendance.getStatus(),
                attendance == null ? null : attendance.getRemarks()
        );
    }

    private static ClassAttendanceResponse toClassResponse(
            Batch batch,
            LocalDate date,
            List<AttendanceResponse> records
    ) {
        return new ClassAttendanceResponse(
                batch.getId(),
                batch.getCode(),
                batch.getName(),
                date,
                records
        );
    }
}
