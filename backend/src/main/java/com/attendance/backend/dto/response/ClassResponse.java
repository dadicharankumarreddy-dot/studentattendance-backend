package com.attendance.backend.dto.response;

import com.attendance.backend.entity.CourseClass;

import java.time.LocalDateTime;

public class ClassResponse {

    private Long id;
    private String className;
    private String subjectName;
    private String description;
    private String classCode;
    private Long teacherId;
    private String teacherName;
    private long studentCount;
    private long sessionCount;
    private LocalDateTime createdAt;

    public ClassResponse() {
    }

    public ClassResponse(Long id, String className, String subjectName, String description,
                         String classCode, Long teacherId, String teacherName,
                         long studentCount, long sessionCount, LocalDateTime createdAt) {
        this.id = id;
        this.className = className;
        this.subjectName = subjectName;
        this.description = description;
        this.classCode = classCode;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.studentCount = studentCount;
        this.sessionCount = sessionCount;
        this.createdAt = createdAt;
    }

    public static ClassResponse fromEntity(CourseClass courseClass, long studentCount, long sessionCount) {
        if (courseClass == null) {
            return null;
        }
        return new ClassResponse(
                courseClass.getId(),
                courseClass.getClassName(),
                courseClass.getSubjectName(),
                courseClass.getDescription(),
                courseClass.getClassCode(),
                courseClass.getTeacher() != null ? courseClass.getTeacher().getId() : null,
                courseClass.getTeacher() != null ? courseClass.getTeacher().getName() : null,
                studentCount,
                sessionCount,
                courseClass.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getClassCode() {
        return classCode;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public long getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(long studentCount) {
        this.studentCount = studentCount;
    }

    public long getSessionCount() {
        return sessionCount;
    }

    public void setSessionCount(long sessionCount) {
        this.sessionCount = sessionCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
