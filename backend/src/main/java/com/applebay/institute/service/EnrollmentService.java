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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final BatchRepository batchRepository;

    public EnrollmentService(
            EnrollmentRepository enrollmentRepository,
            StudentRepository studentRepository,
            CourseRepository courseRepository,
            BatchRepository batchRepository
    ) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.batchRepository = batchRepository;
    }

    @Transactional
    public EnrollmentResponse createEnrollment(EnrollmentRequest request) {
        Student student = findStudent(request.getStudentId());
        Course course = findCourse(request.getCourseId());
        Batch batch = findBatch(request.getBatchId());

        if (!batch.getCourse().getId().equals(course.getId())) {
            throw new BusinessRuleException("The selected batch does not belong to the selected course");
        }

        if (enrollmentRepository.existsByStudent_IdAndCourse_IdAndBatch_Id(
                student.getId(),
                course.getId(),
                batch.getId()
        )) {
            throw new DuplicateResourceException(
                    "The student is already enrolled in this course and batch"
            );
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setBatch(batch);
        enrollment.setEnrolledOn(LocalDate.now());
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        return toResponse(enrollmentRepository.save(enrollment));
    }

    @Transactional(readOnly = true)
    public EnrollmentResponse getEnrollment(Long id) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment " + id + " was not found"));
        return toResponse(enrollment);
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getStudentEnrollments(Long studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student " + studentId + " was not found");
        }
        return enrollmentRepository.findAllByStudent_IdOrderByEnrolledOnDesc(studentId)
                .stream()
                .map(EnrollmentService::toResponse)
                .toList();
    }

    private Student findStudent(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student " + id + " was not found"));
    }

    private Course findCourse(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course " + id + " was not found"));
    }

    private Batch findBatch(Long id) {
        return batchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Batch " + id + " was not found"));
    }

    private static EnrollmentResponse toResponse(Enrollment enrollment) {
        Student student = enrollment.getStudent();
        Course course = enrollment.getCourse();
        Batch batch = enrollment.getBatch();
        return new EnrollmentResponse(
                enrollment.getId(),
                student.getId(),
                student.getStudentNumber(),
                student.getFirstName(),
                student.getLastName(),
                course.getId(),
                course.getCode(),
                course.getName(),
                batch.getId(),
                batch.getCode(),
                batch.getName(),
                enrollment.getEnrolledOn(),
                enrollment.getStatus()
        );
    }
}
