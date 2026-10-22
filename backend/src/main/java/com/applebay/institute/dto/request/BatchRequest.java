package com.applebay.institute.dto.request;

import com.applebay.institute.entity.BatchStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class BatchRequest {

    @NotBlank
    @Size(max = 30)
    private String code;

    @NotBlank
    @Size(max = 120)
    private String name;

    @NotNull
    @Positive
    private Long courseId;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @NotNull
    @Positive
    private Integer capacity;

    private BatchStatus status;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public BatchStatus getStatus() {
        return status;
    }

    public void setStatus(BatchStatus status) {
        this.status = status;
    }
}
