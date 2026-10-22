package com.applebay.institute.repository;

import com.applebay.institute.entity.Batch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BatchRepository extends JpaRepository<Batch, Long> {

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    List<Batch> findAllByCourse_Id(Long courseId);
}
