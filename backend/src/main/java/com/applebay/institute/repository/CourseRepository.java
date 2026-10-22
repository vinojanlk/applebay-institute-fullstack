package com.applebay.institute.repository;

import com.applebay.institute.entity.Course;
import com.applebay.institute.entity.CourseStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {

    long countByStatus(CourseStatus status);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);
}
