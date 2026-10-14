package com.attendance.backend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class MarkAttendanceRequest {

    @NotEmpty(message = "Attendance records list must not be empty")
    @Valid
    private List<AttendanceRecordRequest> records;

    public MarkAttendanceRequest() {
    }

    public MarkAttendanceRequest(List<AttendanceRecordRequest> records) {
        this.records = records;
    }

    public List<AttendanceRecordRequest> getRecords() {
        return records;
    }

    public void setRecords(List<AttendanceRecordRequest> records) {
        this.records = records;
    }
}
