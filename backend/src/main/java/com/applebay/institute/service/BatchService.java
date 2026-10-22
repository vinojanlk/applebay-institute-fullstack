package com.applebay.institute.service;

import com.applebay.institute.dto.request.BatchRequest;
import com.applebay.institute.dto.response.BatchResponse;
import com.applebay.institute.entity.Batch;
import com.applebay.institute.entity.BatchStatus;
import com.applebay.institute.entity.Course;
import com.applebay.institute.exception.BusinessRuleException;
import com.applebay.institute.exception.DuplicateResourceException;
import com.applebay.institute.exception.ResourceNotFoundException;
import com.applebay.institute.repository.BatchRepository;
import com.applebay.institute.repository.CourseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BatchService {

    private final BatchRepository batchRepository;
    private final CourseRepository courseRepository;

    public BatchService(BatchRepository batchRepository, CourseRepository courseRepository) {
        this.batchRepository = batchRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional(readOnly = true)
    public List<BatchResponse> getBatches(Long courseId) {
        List<Batch> batches;
        if (courseId == null) {
            batches = batchRepository.findAll();
        } else {
            if (!courseRepository.existsById(courseId)) {
                throw new ResourceNotFoundException("Course " + courseId + " was not found");
            }
            batches = batchRepository.findAllByCourse_Id(courseId);
        }
        return batches.stream().map(BatchService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public BatchResponse getBatch(Long id) {
        return toResponse(findBatch(id));
    }

    @Transactional
    public BatchResponse createBatch(BatchRequest request) {
        validateDateRange(request);
        Course course = findCourse(request.getCourseId());
        if (batchRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException(
                    "A batch with code " + request.getCode() + " already exists"
            );
        }

        Batch batch = new Batch();
        updateBatchFields(batch, request, course);
        batch.setStatus(request.getStatus() == null ? BatchStatus.PLANNED : request.getStatus());
        return toResponse(batchRepository.save(batch));
    }

    @Transactional
    public BatchResponse updateBatch(Long id, BatchRequest request) {
        validateDateRange(request);
        Batch batch = findBatch(id);
        Course course = findCourse(request.getCourseId());
        if (batchRepository.existsByCodeAndIdNot(request.getCode(), id)) {
            throw new DuplicateResourceException(
                    "A batch with code " + request.getCode() + " already exists"
            );
        }

        updateBatchFields(batch, request, course);
        if (request.getStatus() != null) {
            batch.setStatus(request.getStatus());
        }
        return toResponse(batchRepository.save(batch));
    }

    private void validateDateRange(BatchRequest request) {
        if (!request.getEndDate().isAfter(request.getStartDate())) {
            throw new BusinessRuleException("Batch end date must be after its start date");
        }
    }

    private Course findCourse(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course " + id + " was not found"));
    }

    private Batch findBatch(Long id) {
        return batchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Batch " + id + " was not found"));
    }

    private void updateBatchFields(Batch batch, BatchRequest request, Course course) {
        batch.setCode(request.getCode());
        batch.setName(request.getName());
        batch.setCourse(course);
        batch.setStartDate(request.getStartDate());
        batch.setEndDate(request.getEndDate());
        batch.setCapacity(request.getCapacity());
    }

    private static BatchResponse toResponse(Batch batch) {
        return new BatchResponse(
                batch.getId(),
                batch.getCode(),
                batch.getName(),
                batch.getCourse().getId(),
                batch.getCourse().getCode(),
                batch.getCourse().getName(),
                batch.getStartDate(),
                batch.getEndDate(),
                batch.getCapacity(),
                batch.getStatus()
        );
    }
}
