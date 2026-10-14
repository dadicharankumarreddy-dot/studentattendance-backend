package com.attendance.backend.dto.response;

import com.attendance.backend.entity.AttendanceRecord;
import com.attendance.backend.entity.AttendanceStatus;

import java.time.LocalDateTime;

public class AttendanceRecordResponse {

    private Long id;
    private Long sessionId;
    private Long classId;
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private AttendanceStatus status;
    private LocalDateTime markedAt;
    private LocalDateTime updatedAt;

    public AttendanceRecordResponse() {
    }

    public AttendanceRecordResponse(Long id, Long sessionId, Long classId, Long studentId,
                                    String studentName, String studentEmail, AttendanceStatus status,
                                    LocalDateTime markedAt, LocalDateTime updatedAt) {
        this.id = id;
        this.sessionId = sessionId;
        this.classId = classId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.status = status;
        this.markedAt = markedAt;
        this.updatedAt = updatedAt;
    }

    public static AttendanceRecordResponse fromEntity(AttendanceRecord record) {
        if (record == null) {
            return null;
        }
        return new AttendanceRecordResponse(
                record.getId(),
                record.getAttendanceSession() != null ? record.getAttendanceSession().getId() : null,
                record.getAttendanceSession() != null && record.getAttendanceSession().getCourseClass() != null
                        ? record.getAttendanceSession().getCourseClass().getId() : null,
                record.getStudent() != null ? record.getStudent().getId() : null,
                record.getStudent() != null ? record.getStudent().getName() : null,
                record.getStudent() != null ? record.getStudent().getEmail() : null,
                record.getStatus(),
                record.getMarkedAt(),
                record.getUpdatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }

    public LocalDateTime getMarkedAt() {
        return markedAt;
    }

    public void setMarkedAt(LocalDateTime markedAt) {
        this.markedAt = markedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
