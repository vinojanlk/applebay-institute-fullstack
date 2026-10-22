package com.applebay.institute.dto.response;

import com.applebay.institute.entity.BatchStatus;

import java.time.LocalDate;

public class BatchResponse {

    private final Long id;
    private final String code;
    private final String name;
    private final Long courseId;
    private final String courseCode;
    private final String courseName;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final int capacity;
    private final BatchStatus status;

    public BatchResponse(
            Long id,
            String code,
            String name,
            Long courseId,
            String courseCode,
            String courseName,
            LocalDate startDate,
            LocalDate endDate,
            int capacity,
            BatchStatus status
    ) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.capacity = capacity;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public Long getCourseId() {
        return courseId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public int getCapacity() {
        return capacity;
    }

    public BatchStatus getStatus() {
        return status;
    }
}
