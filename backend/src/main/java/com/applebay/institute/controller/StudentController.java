package com.applebay.institute.controller;

import com.applebay.institute.dto.request.StudentRegistrationRequest;
import com.applebay.institute.dto.response.StudentResponse;
import com.applebay.institute.dto.response.StudentSearchResponse;
import com.applebay.institute.service.StudentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StudentResponse registerStudent(
            @Valid @RequestBody StudentRegistrationRequest request
    ) {
        return studentService.registerStudent(request);
    }

    @GetMapping("/{id}")
    public StudentResponse getStudent(@PathVariable @Min(1) Long id) {
        return studentService.getStudent(id);
    }

    @GetMapping("/search")
    public StudentSearchResponse searchStudents(
            @RequestParam @NotBlank @Size(max = 100) String query,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size
    ) {
        return studentService.searchStudents(query, page, size);
    }
}
