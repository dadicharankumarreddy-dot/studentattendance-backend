package com.attendance.backend.dto.response;

import com.attendance.backend.entity.AttendanceSession;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AttendanceSessionResponse {

    private Long id;
    private Long classId;
    private String className;
    private String subjectName;
    private LocalDate attendanceDate;
    private LocalDateTime createdAt;
    private List<AttendanceRecordResponse> records = new ArrayList<>();
    private long presentCount;
    private long absentCount;
    private long totalEnrolled;

    public AttendanceSessionResponse() {
    }

    public AttendanceSessionResponse(Long id, Long classId, String className, String subjectName,
                                     LocalDate attendanceDate, LocalDateTime createdAt,
                                     List<AttendanceRecordResponse> records,
                                     long presentCount, long absentCount, long totalEnrolled) {
        this.id = id;
        this.classId = classId;
        this.className = className;
        this.subjectName = subjectName;
        this.attendanceDate = attendanceDate;
        this.createdAt = createdAt;
        this.records = records != null ? records : new ArrayList<>();
        this.presentCount = presentCount;
        this.absentCount = absentCount;
        this.totalEnrolled = totalEnrolled;
    }

    public static AttendanceSessionResponse fromEntity(AttendanceSession session,
                                                       List<AttendanceRecordResponse> records,
                                                       long presentCount,
                                                       long absentCount,
                                                       long totalEnrolled) {
        if (session == null) {
            return null;
        }
        return new AttendanceSessionResponse(
                session.getId(),
                session.getCourseClass() != null ? session.getCourseClass().getId() : null,
                session.getCourseClass() != null ? session.getCourseClass().getClassName() : null,
                session.getCourseClass() != null ? session.getCourseClass().getSubjectName() : null,
                session.getAttendanceDate(),
                session.getCreatedAt(),
                records,
                presentCount,
                absentCount,
                totalEnrolled
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(LocalDate attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<AttendanceRecordResponse> getRecords() {
        return records;
    }

    public void setRecords(List<AttendanceRecordResponse> records) {
        this.records = records;
    }

    public long getPresentCount() {
        return presentCount;
    }

    public void setPresentCount(long presentCount) {
        this.presentCount = presentCount;
    }

    public long getAbsentCount() {
        return absentCount;
    }

    public void setAbsentCount(long absentCount) {
        this.absentCount = absentCount;
    }

    public long getTotalEnrolled() {
        return totalEnrolled;
    }

    public void setTotalEnrolled(long totalEnrolled) {
        this.totalEnrolled = totalEnrolled;
    }
}
