package com.applebay.institute.service;

import com.applebay.institute.dto.request.CourseRequest;
import com.applebay.institute.dto.response.CourseResponse;
import com.applebay.institute.entity.Course;
import com.applebay.institute.entity.CourseLevel;
import com.applebay.institute.entity.CourseStatus;
import com.applebay.institute.exception.DuplicateResourceException;
import com.applebay.institute.exception.ResourceNotFoundException;
import com.applebay.institute.repository.CourseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseService courseService;

    @Test
    void newCourseDefaultsToActive() {
        CourseRequest request = courseRequest();
        when(courseRepository.existsByCode(request.getCode())).thenReturn(false);
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CourseResponse response = courseService.createCourse(request);

        assertEquals(CourseStatus.ACTIVE, response.getStatus());
        assertEquals(3, response.getDurationMonths());
    }

    @Test
    void deactivationChangesCourseStatusToInactive() {
        Course course = course();
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseRepository.save(course)).thenReturn(course);

        CourseResponse response = courseService.deactivateCourse(1L);

        assertEquals(CourseStatus.INACTIVE, response.getStatus());
        verify(courseRepository).save(course);
    }

    @Test
    void deactivationOfMissingCourseThrowsNotFound() {
        when(courseRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> courseService.deactivateCourse(1L));
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void duplicateCourseCodeIsRejected() {
        CourseRequest request = courseRequest();
        when(courseRepository.existsByCode(request.getCode())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> courseService.createCourse(request));
        verify(courseRepository, never()).save(any(Course.class));
    }

    private CourseRequest courseRequest() {
        CourseRequest request = new CourseRequest();
        request.setCode("WEB-101");
        request.setName("Web Development");
        request.setDescription("Website basics");
        request.setFee(new BigDecimal("450.00"));
        request.setLevel(CourseLevel.HND);
        request.setDurationMonths(3);
        return request;
    }

    private Course course() {
        Course course = new Course();
        course.setId(1L);
        course.setCode("WEB-101");
        course.setName("Web Development");
        course.setDescription("Website basics");
        course.setFee(new BigDecimal("450.00"));
        course.setLevel(CourseLevel.HND);
        course.setDurationMonths(3);
        course.setStatus(CourseStatus.ACTIVE);
        return course;
    }
}
