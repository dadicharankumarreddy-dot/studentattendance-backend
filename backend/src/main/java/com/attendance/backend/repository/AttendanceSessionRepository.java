package com.attendance.backend.repository;

import com.attendance.backend.entity.AttendanceSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {

    List<AttendanceSession> findByCourseClassIdOrderByAttendanceDateDesc(Long classId);

    Optional<AttendanceSession> findByCourseClassIdAndAttendanceDate(Long classId, LocalDate attendanceDate);

    boolean existsByCourseClassIdAndAttendanceDate(Long classId, LocalDate attendanceDate);

    long countByCourseClassId(Long classId);
}
