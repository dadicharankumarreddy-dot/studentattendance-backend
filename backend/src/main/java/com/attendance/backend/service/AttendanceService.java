package com.attendance.backend.service;

import com.attendance.backend.dto.request.AttendanceRecordRequest;
import com.attendance.backend.dto.request.CreateAttendanceSessionRequest;
import com.attendance.backend.dto.request.MarkAttendanceRequest;
import com.attendance.backend.dto.response.AttendanceRecordResponse;
import com.attendance.backend.dto.response.AttendanceSessionResponse;
import com.attendance.backend.dto.response.AttendanceSummaryResponse;
import com.attendance.backend.entity.*;
import com.attendance.backend.exception.DuplicateAttendanceSessionException;
import com.attendance.backend.exception.ResourceNotFoundException;
import com.attendance.backend.exception.StudentNotEnrolledException;
import com.attendance.backend.exception.UnauthorizedAccessException;
import com.attendance.backend.repository.AttendanceRecordRepository;
import com.attendance.backend.repository.AttendanceSessionRepository;
import com.attendance.backend.repository.EnrollmentRepository;
import com.attendance.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    private final AttendanceSessionRepository attendanceSessionRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseClassService courseClassService;
    private final UserRepository userRepository;

    public AttendanceService(
            AttendanceSessionRepository attendanceSessionRepository,
            AttendanceRecordRepository attendanceRecordRepository,
            EnrollmentRepository enrollmentRepository,
            CourseClassService courseClassService,
            UserRepository userRepository
    ) {
        this.attendanceSessionRepository = attendanceSessionRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.courseClassService = courseClassService;
        this.userRepository = userRepository;
    }

    private User getStudentByEmail(String email) {
        User student = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with email: " + email));
        if (student.getRole() != Role.STUDENT) {
            throw new UnauthorizedAccessException("Only students can access student attendance records");
        }
        return student;
    }

    @Transactional
    public AttendanceSessionResponse createSession(Long classId, CreateAttendanceSessionRequest request, String teacherEmail) {
        CourseClass courseClass = courseClassService.getVerifiedClassForTeacher(classId, teacherEmail);

        if (attendanceSessionRepository.existsByCourseClassIdAndAttendanceDate(courseClass.getId(), request.getAttendanceDate())) {
            throw new DuplicateAttendanceSessionException(
                    "An attendance session for class '" + courseClass.getClassName() +
                            "' on date " + request.getAttendanceDate() + " already exists"
            );
        }

        AttendanceSession session = new AttendanceSession(courseClass, request.getAttendanceDate());
        AttendanceSession savedSession = attendanceSessionRepository.save(session);

        long totalEnrolled = enrollmentRepository.countByCourseClassId(courseClass.getId());

        return AttendanceSessionResponse.fromEntity(
                savedSession,
                new ArrayList<>(),
                0,
                0,
                totalEnrolled
        );
    }

    @Transactional(readOnly = true)
    public List<AttendanceSessionResponse> getSessionsForClass(Long classId, String teacherEmail) {
        CourseClass courseClass = courseClassService.getVerifiedClassForTeacher(classId, teacherEmail);
        List<AttendanceSession> sessions = attendanceSessionRepository.findByCourseClassIdOrderByAttendanceDateDesc(courseClass.getId());
        long totalEnrolled = enrollmentRepository.countByCourseClassId(courseClass.getId());

        return sessions.stream().map(session -> {
            List<AttendanceRecord> records = attendanceRecordRepository.findByAttendanceSessionId(session.getId());
            long presentCount = records.stream().filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
            long absentCount = records.stream().filter(r -> r.getStatus() == AttendanceStatus.ABSENT).count();
            List<AttendanceRecordResponse> recordResponses = records.stream()
                    .map(AttendanceRecordResponse::fromEntity)
                    .collect(Collectors.toList());

            return AttendanceSessionResponse.fromEntity(
                    session,
                    recordResponses,
                    presentCount,
                    absentCount,
                    totalEnrolled
            );
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AttendanceSessionResponse getSessionById(Long sessionId, String teacherEmail) {
        AttendanceSession session = attendanceSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance session not found with id: " + sessionId));

        CourseClass courseClass = session.getCourseClass();
        courseClassService.getVerifiedClassForTeacher(courseClass.getId(), teacherEmail);

        List<AttendanceRecord> records = attendanceRecordRepository.findByAttendanceSessionId(session.getId());
        long presentCount = records.stream().filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
        long absentCount = records.stream().filter(r -> r.getStatus() == AttendanceStatus.ABSENT).count();
        long totalEnrolled = enrollmentRepository.countByCourseClassId(courseClass.getId());

        List<AttendanceRecordResponse> recordResponses = records.stream()
                .map(AttendanceRecordResponse::fromEntity)
                .collect(Collectors.toList());

        return AttendanceSessionResponse.fromEntity(
                session,
                recordResponses,
                presentCount,
                absentCount,
                totalEnrolled
        );
    }

    @Transactional
    public AttendanceSessionResponse markOrUpdateAttendance(Long sessionId, MarkAttendanceRequest request, String teacherEmail) {
        AttendanceSession session = attendanceSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance session not found with id: " + sessionId));

        CourseClass courseClass = session.getCourseClass();
        courseClassService.getVerifiedClassForTeacher(courseClass.getId(), teacherEmail);

        for (AttendanceRecordRequest recReq : request.getRecords()) {
            boolean isEnrolled = enrollmentRepository.existsByCourseClassIdAndStudentId(courseClass.getId(), recReq.getStudentId());
            if (!isEnrolled) {
                throw new StudentNotEnrolledException(
                        "Student with ID " + recReq.getStudentId() + " is not enrolled in class '" + courseClass.getClassName() + "'"
                );
            }

            User student = userRepository.findById(recReq.getStudentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + recReq.getStudentId()));

            Optional<AttendanceRecord> existingOpt = attendanceRecordRepository.findByAttendanceSessionIdAndStudentId(
                    session.getId(),
                    student.getId()
            );

            if (existingOpt.isPresent()) {
                AttendanceRecord existing = existingOpt.get();
                existing.setStatus(recReq.getStatus());
                attendanceRecordRepository.save(existing);
            } else {
                AttendanceRecord newRecord = new AttendanceRecord(session, student, recReq.getStatus());
                attendanceRecordRepository.save(newRecord);
            }
        }

        List<AttendanceRecord> allRecords = attendanceRecordRepository.findByAttendanceSessionId(session.getId());
        long presentCount = allRecords.stream().filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
        long absentCount = allRecords.stream().filter(r -> r.getStatus() == AttendanceStatus.ABSENT).count();
        long totalEnrolled = enrollmentRepository.countByCourseClassId(courseClass.getId());

        List<AttendanceRecordResponse> recordResponses = allRecords.stream()
                .map(AttendanceRecordResponse::fromEntity)
                .collect(Collectors.toList());

        return AttendanceSessionResponse.fromEntity(
                session,
                recordResponses,
                presentCount,
                absentCount,
                totalEnrolled
        );
    }

    @Transactional(readOnly = true)
    public List<AttendanceRecordResponse> getStudentClassAttendance(Long classId, String studentEmail) {
        User student = getStudentByEmail(studentEmail);

        boolean isEnrolled = enrollmentRepository.existsByCourseClassIdAndStudentId(classId, student.getId());
        if (!isEnrolled) {
            throw new UnauthorizedAccessException("You are not enrolled in class with id: " + classId);
        }

        List<AttendanceRecord> records = attendanceRecordRepository
                .findByAttendanceSessionCourseClassIdAndStudentIdOrderByAttendanceSessionAttendanceDateDesc(classId, student.getId());

        return records.stream()
                .map(AttendanceRecordResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AttendanceSummaryResponse getStudentClassSummary(Long classId, String studentEmail) {
        User student = getStudentByEmail(studentEmail);

        Enrollment enrollment = enrollmentRepository.findByCourseClassIdAndStudentId(classId, student.getId())
                .orElseThrow(() -> new UnauthorizedAccessException("You are not enrolled in class with id: " + classId));

        CourseClass courseClass = enrollment.getCourseClass();
        long totalClasses = attendanceSessionRepository.countByCourseClassId(classId);
        long presentCount = attendanceRecordRepository.countByAttendanceSessionCourseClassIdAndStudentIdAndStatus(
                classId,
                student.getId(),
                AttendanceStatus.PRESENT
        );
        long absentCount = attendanceRecordRepository.countByAttendanceSessionCourseClassIdAndStudentIdAndStatus(
                classId,
                student.getId(),
                AttendanceStatus.ABSENT
        );

        double percentage = 0.0;
        if (totalClasses > 0) {
            double raw = ((double) presentCount / (double) totalClasses) * 100.0;
            percentage = BigDecimal.valueOf(raw)
                    .setScale(1, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        return new AttendanceSummaryResponse(
                courseClass.getId(),
                courseClass.getClassName(),
                courseClass.getSubjectName(),
                totalClasses,
                presentCount,
                absentCount,
                percentage
        );
    }

    @Transactional(readOnly = true)
    public List<AttendanceRecordResponse> getStudentAllAttendance(String studentEmail) {
        User student = getStudentByEmail(studentEmail);
        List<AttendanceRecord> records = attendanceRecordRepository.findByStudentIdOrderByAttendanceSessionAttendanceDateDesc(student.getId());

        return records.stream()
                .map(AttendanceRecordResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AttendanceSummaryResponse> getStudentAllSummaries(String studentEmail) {
        User student = getStudentByEmail(studentEmail);
        List<Enrollment> enrollments = enrollmentRepository.findByStudentIdOrderByEnrolledAtDesc(student.getId());

        return enrollments.stream().map(enrollment -> {
            CourseClass courseClass = enrollment.getCourseClass();
            long totalClasses = attendanceSessionRepository.countByCourseClassId(courseClass.getId());
            long presentCount = attendanceRecordRepository.countByAttendanceSessionCourseClassIdAndStudentIdAndStatus(
                    courseClass.getId(),
                    student.getId(),
                    AttendanceStatus.PRESENT
            );
            long absentCount = attendanceRecordRepository.countByAttendanceSessionCourseClassIdAndStudentIdAndStatus(
                    courseClass.getId(),
                    student.getId(),
                    AttendanceStatus.ABSENT
            );

            double percentage = 0.0;
            if (totalClasses > 0) {
                double raw = ((double) presentCount / (double) totalClasses) * 100.0;
                percentage = BigDecimal.valueOf(raw)
                        .setScale(1, RoundingMode.HALF_UP)
                        .doubleValue();
            }

            return new AttendanceSummaryResponse(
                    courseClass.getId(),
                    courseClass.getClassName(),
                    courseClass.getSubjectName(),
                    totalClasses,
                    presentCount,
                    absentCount,
                    percentage
            );
        }).collect(Collectors.toList());
    }
}
