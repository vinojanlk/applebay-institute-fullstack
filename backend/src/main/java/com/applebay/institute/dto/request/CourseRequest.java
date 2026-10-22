package com.applebay.institute.dto.request;

import com.applebay.institute.entity.CourseLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class CourseRequest {

    @NotBlank
    @Size(max = 30)
    private String code;

    @NotBlank
    @Size(max = 120)
    private String name;

    @Size(max = 2000)
    private String description;

    @NotNull
    @Positive
    private BigDecimal fee;

    @NotNull
    private CourseLevel level;

    @NotNull
    @Positive
    private Integer durationMonths;

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

    public Integer getDurationMonths() {
        return durationMonths;
    }

    public void setDurationMonths(Integer durationMonths) {
        this.durationMonths = durationMonths;
    }
}
