package com.applebay.institute.service;

import com.applebay.institute.dto.request.StudentEnrollmentPaymentRequest;
import com.applebay.institute.dto.request.StudentRegistrationRequest;
import com.applebay.institute.dto.response.StudentEnrollmentPaymentResponse;
import com.applebay.institute.entity.Batch;
import com.applebay.institute.entity.BatchStatus;
import com.applebay.institute.entity.Course;
import com.applebay.institute.entity.CourseLevel;
import com.applebay.institute.entity.CourseStatus;
import com.applebay.institute.entity.PaymentMethod;
import com.applebay.institute.exception.BusinessRuleException;
import com.applebay.institute.exception.DuplicateResourceException;
import com.applebay.institute.exception.ResourceNotFoundException;
import com.applebay.institute.repository.BatchRepository;
import com.applebay.institute.repository.CourseRepository;
import com.applebay.institute.repository.EnrollmentRepository;
import com.applebay.institute.repository.PaymentRepository;
import com.applebay.institute.repository.StudentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:student-enrollment-payment;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class StudentEnrollmentPaymentServiceIntegrationTest {

    @Autowired
    private StudentEnrollmentPaymentService workflowService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private BatchRepository batchRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long courseId;
    private Long batchId;
    private String courseCode;
    private String batchCode;
    private String studentEmail;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("CREATE SEQUENCE IF NOT EXISTS student_number_seq START WITH 1");
        jdbcTemplate.execute("CREATE SEQUENCE IF NOT EXISTS payment_receipt_seq START WITH 1");

        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        courseCode = "WF-" + suffix;
        batchCode = "WF-B-" + suffix;

        Course course = new Course();
        course.setCode(courseCode);
        course.setName("Workflow Test Course");
        course.setFee(new BigDecimal("100.00"));
        course.setLevel(CourseLevel.HND);
        course.setDurationMonths(3);
        course.setStatus(CourseStatus.ACTIVE);
        courseId = courseRepository.save(course).getId();

        Batch batch = new Batch();
        batch.setCode(batchCode);
        batch.setName("Workflow Test Batch");
        batch.setCourse(course);
        batch.setStartDate(LocalDate.now());
        batch.setEndDate(LocalDate.now().plusMonths(3));
        batch.setCapacity(25);
        batch.setStatus(BatchStatus.PLANNED);
        batchId = batchRepository.save(batch).getId();
    }

    @AfterEach
    void cleanUp() {
        if (courseId != null) {
            jdbcTemplate.update(
                    "DELETE FROM payments WHERE enrollment_id IN "
                            + "(SELECT id FROM enrollments WHERE course_id = ?)",
                    courseId
            );
            jdbcTemplate.update(
                    "DELETE FROM attendance WHERE enrollment_id IN "
                            + "(SELECT id FROM enrollments WHERE course_id = ?)",
                    courseId
            );
            jdbcTemplate.update("DELETE FROM enrollments WHERE course_id = ?", courseId);
        }
        if (studentEmail != null) {
            jdbcTemplate.update("DELETE FROM students WHERE email = ?", studentEmail);
        }
        if (batchId != null) {
            jdbcTemplate.update("DELETE FROM batches WHERE id = ?", batchId);
        }
        if (courseId != null) {
            jdbcTemplate.update("DELETE FROM courses WHERE id = ?", courseId);
        }
    }

    @Test
    void createsStudentEnrollmentAndPaymentTogether() {
        long[] before = rowCounts();

        StudentEnrollmentPaymentResponse response =
                workflowService.registerEnrollAndPay(request("success"));

        assertNotNull(response.getStudent().getId());
        assertNotNull(response.getEnrollment().getId());
        assertNotNull(response.getPayment().getId());
        assertEquals(response.getStudent().getId(), response.getEnrollment().getStudentId());
        assertEquals(response.getEnrollment().getId(), response.getPayment().getEnrollmentId());
        assertEquals(1, studentRepository.count());
        assertEquals(1, enrollmentRepository.count());
        assertEquals(1, paymentRepository.count());
        assertEquals(before[0] + 1, rowCounts()[0]);
        assertEquals(before[1] + 1, rowCounts()[1]);
        assertEquals(before[2] + 1, rowCounts()[2]);
    }

    @Test
    void rejectsDuplicateStudentBeforeCreatingEnrollmentOrPayment() {
        StudentEnrollmentPaymentRequest request = request("duplicate");
        studentService.registerStudent(request.getStudent());
        long[] before = rowCounts();

        assertThrows(
                DuplicateResourceException.class,
                () -> workflowService.registerEnrollAndPay(request)
        );

        assertCountsUnchanged(before);
    }

    @Test
    void rollsBackStudentWhenEnrollmentFails() {
        StudentEnrollmentPaymentRequest request = request("enrollment-failure");
        request.setBatchId(Long.MAX_VALUE);
        long[] before = rowCounts();

        assertThrows(
                ResourceNotFoundException.class,
                () -> workflowService.registerEnrollAndPay(request)
        );

        assertCountsUnchanged(before);
    }

    @Test
    void rollsBackStudentAndEnrollmentWhenPaymentIsTooLarge() {
        StudentEnrollmentPaymentRequest request = request("payment-failure");
        request.setAmount(new BigDecimal("100.01"));
        long[] before = rowCounts();

        assertThrows(
                BusinessRuleException.class,
                () -> workflowService.registerEnrollAndPay(request)
        );

        assertCountsUnchanged(before);
    }

    private StudentEnrollmentPaymentRequest request(String suffix) {
        StudentRegistrationRequest student = new StudentRegistrationRequest();
        student.setFirstName("Synthetic Workflow");
        student.setLastName("Test Student");
        student.setNic("WF-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        studentEmail = "workflow-" + suffix + "-" + UUID.randomUUID() + "@example.test";
        student.setEmail(studentEmail);

        StudentEnrollmentPaymentRequest request = new StudentEnrollmentPaymentRequest();
        request.setStudent(student);
        request.setCourseId(courseId);
        request.setBatchId(batchId);
        request.setAmount(new BigDecimal("25.00"));
        request.setPaymentDate(LocalDate.now());
        request.setPaymentMethod(PaymentMethod.CASH);
        return request;
    }

    private long[] rowCounts() {
        return new long[] {
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM students", Long.class),
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM enrollments", Long.class),
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM payments", Long.class)
        };
    }

    private void assertCountsUnchanged(long[] before) {
        long[] after = rowCounts();
        assertEquals(before[0], after[0], "Student row count should be unchanged");
        assertEquals(before[1], after[1], "Enrollment row count should be unchanged");
        assertEquals(before[2], after[2], "Payment row count should be unchanged");
    }
}
