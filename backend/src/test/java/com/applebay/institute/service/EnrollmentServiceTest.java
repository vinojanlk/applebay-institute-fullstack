package com.applebay.institute.service;

import com.applebay.institute.dto.request.EnrollmentRequest;
import com.applebay.institute.dto.response.EnrollmentResponse;
import com.applebay.institute.entity.Batch;
import com.applebay.institute.entity.Course;
import com.applebay.institute.entity.Enrollment;
import com.applebay.institute.entity.EnrollmentStatus;
import com.applebay.institute.entity.Student;
import com.applebay.institute.exception.BusinessRuleException;
import com.applebay.institute.exception.DuplicateResourceException;
import com.applebay.institute.exception.ResourceNotFoundException;
import com.applebay.institute.repository.BatchRepository;
import com.applebay.institute.repository.CourseRepository;
import com.applebay.institute.repository.EnrollmentRepository;
import com.applebay.institute.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private BatchRepository batchRepository;

    @InjectMocks
    private EnrollmentService enrollmentService;

    @Test
    void createsActiveEnrollmentWithCurrentDate() {
        Student student = student();
        Course course = course(2L);
        Batch batch = batch(course);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(2L)).thenReturn(Optional.of(course));
        when(batchRepository.findById(3L)).thenReturn(Optional.of(batch));
        when(enrollmentRepository.existsByStudent_IdAndCourse_IdAndBatch_Id(1L, 2L, 3L))
                .thenReturn(false);
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(invocation -> {
            Enrollment enrollment = invocation.getArgument(0);
            enrollment.setId(4L);
            return enrollment;
        });

        EnrollmentResponse response = enrollmentService.createEnrollment(request());

        assertEquals(4L, response.getId());
        assertEquals(1L, response.getStudentId());
        assertEquals(2L, response.getCourseId());
        assertEquals(3L, response.getBatchId());
        assertEquals(LocalDate.now(), response.getEnrolledOn());
        assertEquals(EnrollmentStatus.ACTIVE, response.getStatus());
        verify(enrollmentRepository).save(any(Enrollment.class));
    }

    @Test
    void missingStudentIsRejected() {
        when(studentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> enrollmentService.createEnrollment(request()));
        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    @Test
    void missingCourseIsRejected() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student()));
        when(courseRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> enrollmentService.createEnrollment(request()));
        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    @Test
    void missingBatchIsRejected() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student()));
        when(courseRepository.findById(2L)).thenReturn(Optional.of(course(2L)));
        when(batchRepository.findById(3L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> enrollmentService.createEnrollment(request()));
        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    @Test
    void batchMustBelongToSelectedCourse() {
        Student student = student();
        Course selectedCourse = course(2L);
        Batch batchForDifferentCourse = batch(course(9L));
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(2L)).thenReturn(Optional.of(selectedCourse));
        when(batchRepository.findById(3L)).thenReturn(Optional.of(batchForDifferentCourse));

        assertThrows(BusinessRuleException.class,
                () -> enrollmentService.createEnrollment(request()));
        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    @Test
    void duplicateEnrollmentIsRejected() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student()));
        when(courseRepository.findById(2L)).thenReturn(Optional.of(course(2L)));
        when(batchRepository.findById(3L)).thenReturn(Optional.of(batch(course(2L))));
        when(enrollmentRepository.existsByStudent_IdAndCourse_IdAndBatch_Id(1L, 2L, 3L))
                .thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> enrollmentService.createEnrollment(request()));
        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    @Test
    void listsStudentEnrollmentsAsResponseDtos() {
        Course course = course(2L);
        Enrollment enrollment = new Enrollment();
        enrollment.setId(4L);
        enrollment.setStudent(student());
        enrollment.setCourse(course);
        enrollment.setBatch(batch(course));
        enrollment.setEnrolledOn(LocalDate.of(2026, 10, 1));
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        when(studentRepository.existsById(1L)).thenReturn(true);
        when(enrollmentRepository.findAllByStudent_IdOrderByEnrolledOnDesc(1L))
                .thenReturn(List.of(enrollment));

        List<EnrollmentResponse> responses = enrollmentService.getStudentEnrollments(1L);

        assertEquals(1, responses.size());
        assertEquals(4L, responses.get(0).getId());
        assertEquals("AB2026-0001", responses.get(0).getStudentNumber());
        assertEquals(EnrollmentStatus.ACTIVE, responses.get(0).getStatus());
    }

    @Test
    void listingEnrollmentsForMissingStudentIsRejected() {
        when(studentRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> enrollmentService.getStudentEnrollments(1L));
    }

    private EnrollmentRequest request() {
        EnrollmentRequest request = new EnrollmentRequest();
        request.setStudentId(1L);
        request.setCourseId(2L);
        request.setBatchId(3L);
        return request;
    }

    private Student student() {
        Student student = new Student();
        student.setId(1L);
        student.setStudentNumber("AB2026-0001");
        student.setFirstName("Jane");
        student.setLastName("Doe");
        return student;
    }

    private Course course(Long id) {
        Course course = new Course();
        course.setId(id);
        course.setCode("WEB-" + id);
        course.setName("Web Development");
        return course;
    }

    private Batch batch(Course course) {
        Batch batch = new Batch();
        batch.setId(3L);
        batch.setCode("WEB-2026-01");
        batch.setName("Web January");
        batch.setCourse(course);
        return batch;
    }
}
