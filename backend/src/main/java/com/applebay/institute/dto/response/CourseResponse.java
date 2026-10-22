package com.applebay.institute.dto.response;

import com.applebay.institute.entity.CourseLevel;
import com.applebay.institute.entity.CourseStatus;

import java.math.BigDecimal;

public class CourseResponse {

    private final Long id;
    private final String code;
    private final String name;
    private final String description;
    private final BigDecimal fee;
    private final CourseLevel level;
    private final int durationMonths;
    private final CourseStatus status;

    public CourseResponse(
            Long id,
            String code,
            String name,
            String description,
            BigDecimal fee,
            CourseLevel level,
            int durationMonths,
            CourseStatus status
    ) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.fee = fee;
        this.level = level;
        this.durationMonths = durationMonths;
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

    public String getDescription() {
        return description;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public CourseLevel getLevel() {
        return level;
    }

    public int getDurationMonths() {
        return durationMonths;
    }

    public CourseStatus getStatus() {
        return status;
    }
}
