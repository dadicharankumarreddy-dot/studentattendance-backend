package com.attendance.backend.dto.response;

import com.attendance.backend.entity.Enrollment;

import java.time.LocalDateTime;

public class EnrollmentResponse {

    private Long id;
    private Long classId;
    private String className;
    private String subjectName;
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private LocalDateTime enrolledAt;

    public EnrollmentResponse() {
    }

    public EnrollmentResponse(Long id, Long classId, String className, String subjectName,
                              Long studentId, String studentName, String studentEmail,
                              LocalDateTime enrolledAt) {
        this.id = id;
        this.classId = classId;
        this.className = className;
        this.subjectName = subjectName;
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.enrolledAt = enrolledAt;
    }

    public static EnrollmentResponse fromEntity(Enrollment enrollment) {
        if (enrollment == null) {
            return null;
        }
        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getCourseClass() != null ? enrollment.getCourseClass().getId() : null,
                enrollment.getCourseClass() != null ? enrollment.getCourseClass().getClassName() : null,
                enrollment.getCourseClass() != null ? enrollment.getCourseClass().getSubjectName() : null,
                enrollment.getStudent() != null ? enrollment.getStudent().getId() : null,
                enrollment.getStudent() != null ? enrollment.getStudent().getName() : null,
                enrollment.getStudent() != null ? enrollment.getStudent().getEmail() : null,
                enrollment.getEnrolledAt()
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

    public LocalDateTime getEnrolledAt() {
        return enrolledAt;
    }

    public void setEnrolledAt(LocalDateTime enrolledAt) {
        this.enrolledAt = enrolledAt;
    }
}
