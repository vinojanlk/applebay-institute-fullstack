package com.applebay.institute.controller;

import com.applebay.institute.dto.request.CourseRequest;
import com.applebay.institute.dto.response.CourseResponse;
import com.applebay.institute.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public List<CourseResponse> getAllCourses() {
        return courseService.getAllCourses();
    }

    @GetMapping("/{id}")
    public CourseResponse getCourse(@PathVariable Long id) {
        return courseService.getCourse(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CourseResponse createCourse(@Valid @RequestBody CourseRequest request) {
        return courseService.createCourse(request);
    }

    @PutMapping("/{id}")
    public CourseResponse updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseRequest request
    ) {
        return courseService.updateCourse(id, request);
    }

    @PatchMapping("/{id}/deactivate")
    public CourseResponse deactivateCourse(@PathVariable Long id) {
        return courseService.deactivateCourse(id);
    }
}
