package com.attendance.backend.dto.request;

import com.attendance.backend.entity.AttendanceStatus;
import jakarta.validation.constraints.NotNull;

public class AttendanceRecordRequest {

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Attendance status is required (PRESENT or ABSENT)")
    private AttendanceStatus status;

    public AttendanceRecordRequest() {
    }

    public AttendanceRecordRequest(Long studentId, AttendanceStatus status) {
        this.studentId = studentId;
        this.status = status;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }
}
