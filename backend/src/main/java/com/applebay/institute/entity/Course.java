package com.applebay.institute.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @NotBlank
    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 2000)
    private String description;

    @NotNull
    @Positive
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal fee;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CourseLevel level;

    @Positive
    @Column(name = "duration_months", nullable = false)
    private int durationMonths;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CourseStatus status = CourseStatus.ACTIVE;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    public CourseLevel getLevel() {
        return level;
    }

    public void setLevel(CourseLevel level) {
        this.level = level;
    }

    public int getDurationMonths() {
        return durationMonths;
    }

    public void setDurationMonths(int durationMonths) {
        this.durationMonths = durationMonths;
    }

    public CourseStatus getStatus() {
        return status;
    }

    public void setStatus(CourseStatus status) {
        this.status = status;
    }
}
