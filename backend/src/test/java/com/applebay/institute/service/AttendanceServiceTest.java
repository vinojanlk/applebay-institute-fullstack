package com.applebay.institute.service;

import com.applebay.institute.dto.request.AttendanceRecordRequest;
import com.applebay.institute.dto.request.ClassAttendanceRequest;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    private static final Long BATCH_ID = 10L;
    private static final LocalDate DATE = LocalDate.of(2026, 10, 19);

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private BatchRepository batchRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    @Test
    void missingBatchIsRejected() {
        when(batchRepository.findById(BATCH_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> attendanceService.getClassAttendance(BATCH_ID, DATE));
        verifyNoInteractions(attendanceRepository, enrollmentRepository);
    }

    @Test
    void classRosterContainsOnlyActiveEnrollments() {
        Batch batch = batch();
        List<Enrollment> activeEnrollments = List.of(enrollment(1L, 101L), enrollment(2L, 102L));
        when(batchRepository.findById(BATCH_ID)).thenReturn(Optional.of(batch));
        when(enrollmentRepository
                .findAllByBatch_IdAndStatusOrderByStudent_LastNameAscStudent_FirstNameAsc(
                        BATCH_ID,
                        EnrollmentStatus.ACTIVE
                )).thenReturn(activeEnrollments);
        when(attendanceRepository.findAllByEnrollment_Batch_IdAndAttendanceDate(BATCH_ID, DATE))
                .thenReturn(List.of());

        ClassAttendanceResponse response = attendanceService.getClassAttendance(BATCH_ID, DATE);

        assertEquals(2, response.getRecords().size());
        assertEquals(1L, response.getRecords().get(0).getEnrollmentId());
        assertEquals(2L, response.getRecords().get(1).getEnrollmentId());
        verify(enrollmentRepository)
                .findAllByBatch_IdAndStatusOrderByStudent_LastNameAscStudent_FirstNameAsc(
                        BATCH_ID,
                        EnrollmentStatus.ACTIVE
                );
    }

    @Test
    void classRosterIncludesExistingAttendanceStatus() {
        Enrollment enrollment = enrollment(1L, 101L);
        Attendance existing = attendance(enrollment, AttendanceStatus.LATE, "Arrived late");
        stubClassRoster(List.of(enrollment), List.of(existing));

        ClassAttendanceResponse response = attendanceService.getClassAttendance(BATCH_ID, DATE);

        assertEquals(AttendanceStatus.LATE, response.getRecords().get(0).getStatus());
        assertEquals("Arrived late", response.getRecords().get(0).getRemarks());
    }

    @Test
    void classRosterShowsNullStatusWhenAttendanceIsNotRecorded() {
        Enrollment enrollment = enrollment(1L, 101L);
        stubClassRoster(List.of(enrollment), List.of());

        ClassAttendanceResponse response = attendanceService.getClassAttendance(BATCH_ID, DATE);

        assertEquals(1, response.getRecords().size());
        assertEquals(null, response.getRecords().get(0).getStatus());
    }

    @Test
    void createsNewAttendanceRecordsForWholeClass() {
        Enrollment first = enrollment(1L, 101L);
        Enrollment second = enrollment(2L, 102L);
        stubSubmission(List.of(first, second));
        when(attendanceRepository.findAllByEnrollment_Batch_IdAndAttendanceDate(BATCH_ID, DATE))
                .thenReturn(List.of());
        when(attendanceRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        ClassAttendanceResponse response =
                attendanceService.submitClassAttendance(request(record(1L, AttendanceStatus.PRESENT),
                        record(2L, AttendanceStatus.ABSENT)));

        assertEquals(2, response.getRecords().size());
        assertEquals(AttendanceStatus.PRESENT, response.getRecords().get(0).getStatus());
        assertEquals(AttendanceStatus.ABSENT, response.getRecords().get(1).getStatus());
        verify(attendanceRepository).saveAll(anyList());
    }

    @Test
    void updatesExistingAttendanceInsteadOfCreatingDuplicate() {
        Enrollment enrollment = enrollment(1L, 101L);
        Attendance existing = attendance(enrollment, AttendanceStatus.ABSENT, "Old remark");
        stubSubmission(List.of(enrollment));
        when(attendanceRepository.findAllByEnrollment_Batch_IdAndAttendanceDate(BATCH_ID, DATE))
                .thenReturn(List.of(existing));
        when(attendanceRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        ClassAttendanceResponse response = attendanceService.submitClassAttendance(
                request(record(1L, AttendanceStatus.PRESENT, "Updated remark"))
        );

        assertEquals(AttendanceStatus.PRESENT, existing.getStatus());
        assertEquals("Updated remark", existing.getRemarks());
        assertEquals(AttendanceStatus.PRESENT, response.getRecords().get(0).getStatus());
        verify(attendanceRepository).saveAll(anyList());
    }

    @Test
    void duplicateEnrollmentIdsInRequestAreRejectedBeforeAttendanceLookup() {
        Enrollment enrollment = enrollment(1L, 101L);
        stubSubmission(List.of(enrollment));

        assertThrows(BusinessRuleException.class, () ->
                attendanceService.submitClassAttendance(
                        request(record(1L, AttendanceStatus.PRESENT),
                                record(1L, AttendanceStatus.ABSENT))
                ));

        verifyNoInteractions(attendanceRepository);
    }

    @Test
    void enrollmentFromAnotherBatchIsRejected() {
        stubSubmission(List.of(enrollment(1L, 101L)));

        assertThrows(BusinessRuleException.class, () ->
                attendanceService.submitClassAttendance(
                        request(record(1L, AttendanceStatus.PRESENT),
                                record(2L, AttendanceStatus.PRESENT))
                ));

        verifyNoInteractions(attendanceRepository);
    }

    @Test
    void inactiveEnrollmentIsRejected() {
        stubSubmission(List.of(enrollment(1L, 101L)));

        assertThrows(BusinessRuleException.class, () ->
                attendanceService.submitClassAttendance(
                        request(record(1L, AttendanceStatus.PRESENT),
                                record(3L, AttendanceStatus.PRESENT))
                ));

        verifyNoInteractions(attendanceRepository);
    }

    @Test
    void missingActiveRosterMemberIsRejected() {
        stubSubmission(List.of(enrollment(1L, 101L), enrollment(2L, 102L)));

        assertThrows(BusinessRuleException.class, () ->
                attendanceService.submitClassAttendance(
                        request(record(1L, AttendanceStatus.PRESENT))
                ));

        verifyNoInteractions(attendanceRepository);
    }

    @Test
    void additionalEnrollmentOutsideRosterIsRejectedBeforeSaving() {
        stubSubmission(List.of(enrollment(1L, 101L)));

        assertThrows(BusinessRuleException.class, () ->
                attendanceService.submitClassAttendance(
                        request(record(1L, AttendanceStatus.PRESENT),
                                record(8L, AttendanceStatus.LATE))
                ));

        verify(attendanceRepository, never()).saveAll(anyList());
        verify(attendanceRepository, never())
                .findAllByEnrollment_Batch_IdAndAttendanceDate(BATCH_ID, DATE);
    }

    @Test
    void validatesCompleteSubmissionBeforeSavingAnyRecords() {
        Enrollment first = enrollment(1L, 101L);
        Enrollment second = enrollment(2L, 102L);
        stubSubmission(List.of(first, second));

        assertThrows(BusinessRuleException.class, () ->
                attendanceService.submitClassAttendance(
                        request(record(1L, AttendanceStatus.PRESENT),
                                record(99L, AttendanceStatus.LATE))
                ));

        verify(attendanceRepository, never()).saveAll(anyList());
        verify(attendanceRepository, never())
                .findAllByEnrollment_Batch_IdAndAttendanceDate(BATCH_ID, DATE);
    }

    @Test
    void failedSaveDoesNotReturnSuccessfulClassResponse() throws NoSuchMethodException {
        Enrollment enrollment = enrollment(1L, 101L);
        stubSubmission(List.of(enrollment));
        when(attendanceRepository.findAllByEnrollment_Batch_IdAndAttendanceDate(BATCH_ID, DATE))
                .thenReturn(List.of());
        doThrow(new IllegalStateException("Database write failed"))
                .when(attendanceRepository).saveAll(anyList());

        assertThrows(IllegalStateException.class, () ->
                attendanceService.submitClassAttendance(
                        request(record(1L, AttendanceStatus.PRESENT))
                ));

        verify(attendanceRepository).saveAll(anyList());
        assertNotNull(AttendanceService.class
                .getMethod("submitClassAttendance", ClassAttendanceRequest.class)
                .getAnnotation(Transactional.class));
    }

    @Test
    void presentCountsAsAttended() {
        assertSummary(List.of(attendance(enrollment(1L, 101L), AttendanceStatus.PRESENT, null)),
                1, 100, true);
    }

    @Test
    void lateCountsAsAttended() {
        assertSummary(List.of(attendance(enrollment(1L, 101L), AttendanceStatus.LATE, null)),
                1, 100, true);
    }

    @Test
    void absentDoesNotCountAsAttended() {
        assertSummary(List.of(attendance(enrollment(1L, 101L), AttendanceStatus.ABSENT, null)),
                0, 0, false);
    }

    @Test
    void calculatesAttendancePercentageCorrectly() {
        Enrollment enrollment = enrollment(1L, 101L);
        List<Attendance> records = List.of(
                attendance(enrollment, AttendanceStatus.PRESENT, null),
                attendance(enrollment, AttendanceStatus.LATE, null),
                attendance(enrollment, AttendanceStatus.ABSENT, null)
        );

        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment));
        when(attendanceRepository.findAllByEnrollment_IdOrderByAttendanceDateAsc(1L))
                .thenReturn(records);

        AttendanceSummaryResponse summary = attendanceService.getStudentSummary(1L);

        assertEquals(new BigDecimal("66.67"), summary.getAttendancePercentage());
        assertEquals(3, summary.getTotalRecords());
        assertEquals(2, summary.getAttendedCount());
    }

    @Test
    void exactlyEightyPercentIsExamEligible() {
        assertSummaryWithCount(List.of(
                AttendanceStatus.PRESENT,
                AttendanceStatus.LATE,
                AttendanceStatus.PRESENT,
                AttendanceStatus.ABSENT,
                AttendanceStatus.ABSENT
        ), new BigDecimal("60.00"), false);
        assertSummaryWithCount(List.of(
                AttendanceStatus.PRESENT,
                AttendanceStatus.LATE,
                AttendanceStatus.PRESENT,
                AttendanceStatus.LATE,
                AttendanceStatus.ABSENT
        ), new BigDecimal("80.00"), true);
    }

    @Test
    void belowEightyPercentIsNotExamEligible() {
        assertSummaryWithCount(List.of(
                AttendanceStatus.PRESENT,
                AttendanceStatus.LATE,
                AttendanceStatus.ABSENT,
                AttendanceStatus.ABSENT
        ), new BigDecimal("50.00"), false);
    }

    @Test
    void noAttendanceRecordsReturnsZeroPercentAndNotEligible() {
        assertSummary(List.of(), 0, 0, false);
    }

    @Test
    void missingEnrollmentIsRejectedForSummary() {
        when(enrollmentRepository.findById(55L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> attendanceService.getStudentSummary(55L));
        verify(attendanceRepository, never()).findAllByEnrollment_IdOrderByAttendanceDateAsc(55L);
    }

    private void assertSummary(List<Attendance> records, long attendedCount, int percentage, boolean eligible) {
        assertSummaryWithCount(
                records.stream().map(Attendance::getStatus).toList(),
                new BigDecimal(String.format("%d.00", percentage)),
                eligible
        );
        assertEquals(attendedCount, records.stream()
                .filter(record -> record.getStatus() == AttendanceStatus.PRESENT
                        || record.getStatus() == AttendanceStatus.LATE)
                .count());
    }

    private void assertSummaryWithCount(
            List<AttendanceStatus> statuses,
            BigDecimal expectedPercentage,
            boolean expectedEligibility
    ) {
        Enrollment enrollment = enrollment(1L, 101L);
        List<Attendance> records = new ArrayList<>();
        for (AttendanceStatus status : statuses) {
            records.add(attendance(enrollment, status, null));
        }
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment));
        when(attendanceRepository.findAllByEnrollment_IdOrderByAttendanceDateAsc(1L))
                .thenReturn(records);

        AttendanceSummaryResponse summary = attendanceService.getStudentSummary(1L);

        assertEquals(expectedPercentage, summary.getAttendancePercentage());
        assertEquals(expectedEligibility, summary.isExamEligible());
    }

    private void stubClassRoster(List<Enrollment> enrollments, List<Attendance> records) {
        when(batchRepository.findById(BATCH_ID)).thenReturn(Optional.of(batch()));
        when(enrollmentRepository
                .findAllByBatch_IdAndStatusOrderByStudent_LastNameAscStudent_FirstNameAsc(
                        BATCH_ID,
                        EnrollmentStatus.ACTIVE
                )).thenReturn(enrollments);
        when(attendanceRepository.findAllByEnrollment_Batch_IdAndAttendanceDate(BATCH_ID, DATE))
                .thenReturn(records);
    }

    private void stubSubmission(List<Enrollment> enrollments) {
        when(batchRepository.findById(BATCH_ID)).thenReturn(Optional.of(batch()));
        when(enrollmentRepository
                .findAllByBatch_IdAndStatusOrderByStudent_LastNameAscStudent_FirstNameAsc(
                        BATCH_ID,
                        EnrollmentStatus.ACTIVE
                )).thenReturn(enrollments);
    }

    private ClassAttendanceRequest request(AttendanceRecordRequest... records) {
        ClassAttendanceRequest request = new ClassAttendanceRequest();
        request.setBatchId(BATCH_ID);
        request.setDate(DATE);
        request.setRecords(List.of(records));
        return request;
    }

    private AttendanceRecordRequest record(Long enrollmentId, AttendanceStatus status) {
        return record(enrollmentId, status, null);
    }

    private AttendanceRecordRequest record(Long enrollmentId, AttendanceStatus status, String remarks) {
        AttendanceRecordRequest record = new AttendanceRecordRequest();
        record.setEnrollmentId(enrollmentId);
        record.setStatus(status);
        record.setRemarks(remarks);
        return record;
    }

    private Batch batch() {
        Batch batch = new Batch();
        batch.setId(BATCH_ID);
        batch.setCode("WEB-2026-01");
        batch.setName("Web January");
        return batch;
    }

    private Enrollment enrollment(Long id, Long studentId) {
        Student student = new Student();
        student.setId(studentId);
        student.setStudentNumber("AB2026-000" + studentId);
        student.setFirstName("First" + studentId);
        student.setLastName("Last" + studentId);

        Enrollment enrollment = new Enrollment();
        enrollment.setId(id);
        enrollment.setStudent(student);
        enrollment.setBatch(batch());
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        return enrollment;
    }

    private Attendance attendance(Enrollment enrollment, AttendanceStatus status, String remarks) {
        Attendance attendance = new Attendance();
        attendance.setId(100L + enrollment.getId());
        attendance.setEnrollment(enrollment);
        attendance.setAttendanceDate(DATE);
        attendance.setStatus(status);
        attendance.setRemarks(remarks);
        return attendance;
    }
}
