package com.applebay.institute.controller;

import com.applebay.institute.dto.request.ClassAttendanceRequest;
import com.applebay.institute.dto.response.AttendanceSummaryResponse;
import com.applebay.institute.dto.response.ClassAttendanceResponse;
import com.applebay.institute.service.AttendanceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Validated
@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping("/class")
    public ClassAttendanceResponse getClassAttendance(
            @RequestParam @Min(1) Long batchId,
            @RequestParam @NotNull LocalDate date
    ) {
        return attendanceService.getClassAttendance(batchId, date);
    }

    @PostMapping("/class")
    public ClassAttendanceResponse submitClassAttendance(
            @Valid @RequestBody ClassAttendanceRequest request
    ) {
        return attendanceService.submitClassAttendance(request);
    }

    @GetMapping("/student/{enrollmentId}/summary")
    public AttendanceSummaryResponse getStudentSummary(
            @PathVariable @Min(1) Long enrollmentId
    ) {
        return attendanceService.getStudentSummary(enrollmentId);
    }
}
