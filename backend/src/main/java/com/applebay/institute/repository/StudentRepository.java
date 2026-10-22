package com.applebay.institute.repository;

import com.applebay.institute.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByNic(String nic);

    boolean existsByEmailIgnoreCase(String email);

    @Query(value = "SELECT nextval('student_number_seq')", nativeQuery = true)
    Long nextStudentNumberValue();

    @Query("""
            SELECT student FROM Student student
            WHERE LOWER(student.firstName) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(student.lastName) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(CONCAT(student.firstName, ' ', student.lastName))
                    LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(student.studentNumber) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(student.nic) LIKE LOWER(CONCAT('%', :query, '%'))
            """)
    Page<Student> searchStudents(@Param("query") String query, Pageable pageable);
}
