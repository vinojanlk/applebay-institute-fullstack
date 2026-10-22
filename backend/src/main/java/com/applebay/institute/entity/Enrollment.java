package com.applebay.institute.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Entity
@Table(
        name = "enrollments",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_enrollments_student_course_batch",
                columnNames = {"student_id", "course_id", "batch_id"}
        )
)
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "batch_id", nullable = false)
    private Batch batch;

    @NotNull
    @Column(name = "enrolled_on", nullable = false)
    private LocalDate enrolledOn;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EnrollmentStatus status = EnrollmentStatus.ACTIVE;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Batch getBatch() {
        return batch;
    }

    public void setBatch(Batch batch) {
        this.batch = batch;
    }

    public LocalDate getEnrolledOn() {
        return enrolledOn;
    }

    public void setEnrolledOn(LocalDate enrolledOn) {
        this.enrolledOn = enrolledOn;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public void setStatus(EnrollmentStatus status) {
        this.status = status;
    }
}
