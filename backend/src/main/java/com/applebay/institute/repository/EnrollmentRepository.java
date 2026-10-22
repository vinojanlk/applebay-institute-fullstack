package com.applebay.institute.repository;

import com.applebay.institute.entity.Enrollment;
import com.applebay.institute.entity.EnrollmentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT enrollment FROM Enrollment enrollment WHERE enrollment.id = :id")
    Optional<Enrollment> findByIdForUpdate(@Param("id") Long id);

    boolean existsByStudent_IdAndCourse_IdAndBatch_Id(
            Long studentId,
            Long courseId,
            Long batchId
    );

    List<Enrollment> findAllByStudent_IdOrderByEnrolledOnDesc(Long studentId);

    List<Enrollment> findAllByBatch_IdAndStatusOrderByStudent_LastNameAscStudent_FirstNameAsc(
            Long batchId,
            EnrollmentStatus status
    );
}
