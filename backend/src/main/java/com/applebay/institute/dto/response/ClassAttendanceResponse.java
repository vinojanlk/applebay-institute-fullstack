package com.applebay.institute.dto.response;

import java.time.LocalDate;
import java.util.List;

public class ClassAttendanceResponse {

    private final Long batchId;
    private final String batchCode;
    private final String batchName;
    private final LocalDate date;
    private final List<AttendanceResponse> records;

    public ClassAttendanceResponse(
            Long batchId,
            String batchCode,
            String batchName,
            LocalDate date,
            List<AttendanceResponse> records
    ) {
        this.batchId = batchId;
        this.batchCode = batchCode;
        this.batchName = batchName;
        this.date = date;
        this.records = records;
    }

    public Long getBatchId() {
        return batchId;
    }

    public String getBatchCode() {
        return batchCode;
    }

    public String getBatchName() {
        return batchName;
    }

    public LocalDate getDate() {
        return date;
    }

    public List<AttendanceResponse> getRecords() {
        return records;
    }
}
