package com.applebay.institute.service;

import com.applebay.institute.dto.request.CourseRequest;
import com.applebay.institute.dto.response.CourseResponse;
import com.applebay.institute.entity.Course;
import com.applebay.institute.entity.CourseStatus;
import com.applebay.institute.exception.DuplicateResourceException;
import com.applebay.institute.exception.ResourceNotFoundException;
import com.applebay.institute.repository.CourseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(CourseService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CourseResponse getCourse(Long id) {
        return toResponse(findCourse(id));
    }

    @Transactional
    public CourseResponse createCourse(CourseRequest request) {
        if (courseRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException(
                    "A course with code " + request.getCode() + " already exists"
            );
        }

        Course course = new Course();
        updateCourseFields(course, request);
        course.setStatus(CourseStatus.ACTIVE);
        return toResponse(courseRepository.save(course));
    }

    @Transactional
    public CourseResponse updateCourse(Long id, CourseRequest request) {
        Course course = findCourse(id);
        if (courseRepository.existsByCodeAndIdNot(request.getCode(), id)) {
            throw new DuplicateResourceException(
                    "A course with code " + request.getCode() + " already exists"
            );
        }

        updateCourseFields(course, request);
        return toResponse(courseRepository.save(course));
    }

    @Transactional
    public CourseResponse deactivateCourse(Long id) {
        Course course = findCourse(id);
        course.setStatus(CourseStatus.INACTIVE);
        return toResponse(courseRepository.save(course));
    }

    private Course findCourse(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course " + id + " was not found"));
    }

    private void updateCourseFields(Course course, CourseRequest request) {
        course.setCode(request.getCode());
        course.setName(request.getName());
        course.setDescription(request.getDescription());
        course.setFee(request.getFee());
        course.setLevel(request.getLevel());
        course.setDurationMonths(request.getDurationMonths());
    }

    private static CourseResponse toResponse(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getCode(),
                course.getName(),
                course.getDescription(),
                course.getFee(),
                course.getLevel(),
                course.getDurationMonths(),
                course.getStatus()
        );
    }
}
