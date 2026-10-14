package com.attendance.backend;

import com.attendance.backend.dto.request.AttendanceRecordRequest;
import com.attendance.backend.dto.request.CreateAttendanceSessionRequest;
import com.attendance.backend.dto.request.MarkAttendanceRequest;
import com.attendance.backend.dto.response.AttendanceSessionResponse;
import com.attendance.backend.dto.response.AttendanceSummaryResponse;
import com.attendance.backend.entity.*;
import com.attendance.backend.exception.DuplicateAttendanceSessionException;
import com.attendance.backend.exception.StudentNotEnrolledException;
import com.attendance.backend.repository.AttendanceRecordRepository;
import com.attendance.backend.repository.AttendanceSessionRepository;
import com.attendance.backend.repository.EnrollmentRepository;
import com.attendance.backend.repository.UserRepository;
import com.attendance.backend.service.AttendanceService;
import com.attendance.backend.service.CourseClassService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AttendanceServiceTest {

    @Mock
    private AttendanceSessionRepository attendanceSessionRepository;

    @Mock
    private AttendanceRecordRepository attendanceRecordRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private CourseClassService courseClassService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    private User teacher;
    private User student;
    private CourseClass sampleClass;
    private AttendanceSession sampleSession;

    @BeforeEach
    void setUp() {
        teacher = new User("Teacher", "teacher@example.com", "pass", Role.TEACHER);
        teacher.setId(1L);

        student = new User("Student", "student@example.com", "pass", Role.STUDENT);
        student.setId(2L);

        sampleClass = new CourseClass("Java 101", "Computer Science", "Desc", "CLS-JAVA101", teacher);
        sampleClass.setId(10L);

        sampleSession = new AttendanceSession(sampleClass, LocalDate.of(2026, 10, 13));
        sampleSession.setId(100L);
    }

    @Test
    void testCreateDuplicateAttendanceSessionThrowsException() {
        LocalDate date = LocalDate.of(2026, 10, 13);
        CreateAttendanceSessionRequest req = new CreateAttendanceSessionRequest(date);

        when(courseClassService.getVerifiedClassForTeacher(10L, "teacher@example.com")).thenReturn(sampleClass);
        when(attendanceSessionRepository.existsByCourseClassIdAndAttendanceDate(10L, date)).thenReturn(true);

        assertThrows(DuplicateAttendanceSessionException.class, () ->
                attendanceService.createSession(10L, req, "teacher@example.com")
        );
        verify(attendanceSessionRepository, never()).save(any(AttendanceSession.class));
    }

    @Test
    void testMarkAttendanceForNonEnrolledStudentThrowsException() {
        MarkAttendanceRequest req = new MarkAttendanceRequest(List.of(
                new AttendanceRecordRequest(2L, AttendanceStatus.PRESENT)
        ));

        when(attendanceSessionRepository.findById(100L)).thenReturn(Optional.of(sampleSession));
        when(courseClassService.getVerifiedClassForTeacher(10L, "teacher@example.com")).thenReturn(sampleClass);
        when(enrollmentRepository.existsByCourseClassIdAndStudentId(10L, 2L)).thenReturn(false);

        assertThrows(StudentNotEnrolledException.class, () ->
                attendanceService.markOrUpdateAttendance(100L, req, "teacher@example.com")
        );
    }

    @Test
    void testAttendancePercentageCalculationWithZeroClasses() {
        Enrollment enrollment = new Enrollment(sampleClass, student);

        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByCourseClassIdAndStudentId(10L, 2L)).thenReturn(Optional.of(enrollment));
        when(attendanceSessionRepository.countByCourseClassId(10L)).thenReturn(0L);
        when(attendanceRecordRepository.countByAttendanceSessionCourseClassIdAndStudentIdAndStatus(10L, 2L, AttendanceStatus.PRESENT)).thenReturn(0L);
        when(attendanceRecordRepository.countByAttendanceSessionCourseClassIdAndStudentIdAndStatus(10L, 2L, AttendanceStatus.ABSENT)).thenReturn(0L);

        AttendanceSummaryResponse summary = attendanceService.getStudentClassSummary(10L, "student@example.com");

        assertNotNull(summary);
        assertEquals(0L, summary.getTotalClasses());
        assertEquals(0L, summary.getPresentCount());
        assertEquals(0.0, summary.getAttendancePercentage());
    }

    @Test
    void testAttendancePercentageCalculationWithValidClasses() {
        Enrollment enrollment = new Enrollment(sampleClass, student);

        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByCourseClassIdAndStudentId(10L, 2L)).thenReturn(Optional.of(enrollment));
        when(attendanceSessionRepository.countByCourseClassId(10L)).thenReturn(10L);
        when(attendanceRecordRepository.countByAttendanceSessionCourseClassIdAndStudentIdAndStatus(10L, 2L, AttendanceStatus.PRESENT)).thenReturn(8L);
        when(attendanceRecordRepository.countByAttendanceSessionCourseClassIdAndStudentIdAndStatus(10L, 2L, AttendanceStatus.ABSENT)).thenReturn(2L);

        AttendanceSummaryResponse summary = attendanceService.getStudentClassSummary(10L, "student@example.com");

        assertNotNull(summary);
        assertEquals(10L, summary.getTotalClasses());
        assertEquals(8L, summary.getPresentCount());
        assertEquals(2L, summary.getAbsentCount());
        assertEquals(80.0, summary.getAttendancePercentage());
    }
}
