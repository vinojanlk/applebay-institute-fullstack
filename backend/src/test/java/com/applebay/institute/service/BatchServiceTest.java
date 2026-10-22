package com.applebay.institute.service;

import com.applebay.institute.dto.request.BatchRequest;
import com.applebay.institute.entity.Course;
import com.applebay.institute.exception.BusinessRuleException;
import com.applebay.institute.exception.DuplicateResourceException;
import com.applebay.institute.exception.ResourceNotFoundException;
import com.applebay.institute.repository.BatchRepository;
import com.applebay.institute.repository.CourseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BatchServiceTest {

    @Mock
    private BatchRepository batchRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private BatchService batchService;

    @Test
    void endDateMustBeAfterStartDate() {
        BatchRequest request = batchRequest();
        request.setEndDate(request.getStartDate());

        assertThrows(BusinessRuleException.class, () -> batchService.createBatch(request));
        verify(batchRepository, never()).save(any());
    }

    @Test
    void referencedCourseMustExist() {
        BatchRequest request = batchRequest();
        when(courseRepository.findById(request.getCourseId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> batchService.createBatch(request));
        verify(batchRepository, never()).save(any());
    }

    @Test
    void duplicateBatchCodeIsRejected() {
        BatchRequest request = batchRequest();
        when(courseRepository.findById(request.getCourseId())).thenReturn(Optional.of(new Course()));
        when(batchRepository.existsByCode(request.getCode())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> batchService.createBatch(request));
        verify(batchRepository, never()).save(any());
    }

    private BatchRequest batchRequest() {
        BatchRequest request = new BatchRequest();
        request.setCode("WEB-101-2026-01");
        request.setName("Web Development January");
        request.setCourseId(1L);
        request.setStartDate(LocalDate.of(2026, 1, 15));
        request.setEndDate(LocalDate.of(2026, 4, 9));
        request.setCapacity(25);
        return request;
    }
}
