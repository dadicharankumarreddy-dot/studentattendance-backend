package com.attendance.backend.repository;

import com.attendance.backend.entity.AttendanceRecord;
import com.attendance.backend.entity.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    List<AttendanceRecord> findByAttendanceSessionId(Long sessionId);

    Optional<AttendanceRecord> findByAttendanceSessionIdAndStudentId(Long sessionId, Long studentId);

    boolean existsByAttendanceSessionIdAndStudentId(Long sessionId, Long studentId);

    List<AttendanceRecord> findByAttendanceSessionCourseClassIdAndStudentIdOrderByAttendanceSessionAttendanceDateDesc(Long classId, Long studentId);

    List<AttendanceRecord> findByStudentIdOrderByAttendanceSessionAttendanceDateDesc(Long studentId);

    long countByAttendanceSessionCourseClassIdAndStudentIdAndStatus(Long classId, Long studentId, AttendanceStatus status);

    long countByAttendanceSessionCourseClassIdAndStudentId(Long classId, Long studentId);
}
