package com.attendance.backend.dto.response;

public class AttendanceSummaryResponse {

    private Long classId;
    private String className;
    private String subjectName;
    private long totalClasses;
    private long presentCount;
    private long absentCount;
    private double attendancePercentage;

    public AttendanceSummaryResponse() {
    }

    public AttendanceSummaryResponse(Long classId, String className, String subjectName,
                                     long totalClasses, long presentCount, long absentCount,
                                     double attendancePercentage) {
        this.classId = classId;
        this.className = className;
        this.subjectName = subjectName;
        this.totalClasses = totalClasses;
        this.presentCount = presentCount;
        this.absentCount = absentCount;
        this.attendancePercentage = attendancePercentage;
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

    public long getTotalClasses() {
        return totalClasses;
    }

    public void setTotalClasses(long totalClasses) {
        this.totalClasses = totalClasses;
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

    public double getAttendancePercentage() {
        return attendancePercentage;
    }

    public void setAttendancePercentage(double attendancePercentage) {
        this.attendancePercentage = attendancePercentage;
    }
}
