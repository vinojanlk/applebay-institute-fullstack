package com.applebay.institute.dto.response;

import java.math.BigDecimal;

public class DashboardResponse {

    private final long totalStudents;
    private final long activeCourses;
    private final long todayPresentCount;
    private final BigDecimal totalFeesCollected;

    public DashboardResponse(
            long totalStudents,
            long activeCourses,
            long todayPresentCount,
            BigDecimal totalFeesCollected
    ) {
        this.totalStudents = totalStudents;
        this.activeCourses = activeCourses;
        this.todayPresentCount = todayPresentCount;
        this.totalFeesCollected = totalFeesCollected;
    }

    public long getTotalStudents() {
        return totalStudents;
    }

    public long getActiveCourses() {
        return activeCourses;
    }

    public long getTodayPresentCount() {
        return todayPresentCount;
    }

    public BigDecimal getTotalFeesCollected() {
        return totalFeesCollected;
    }
}
