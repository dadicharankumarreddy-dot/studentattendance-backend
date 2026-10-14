package com.attendance.backend.controller;

import com.attendance.backend.dto.response.AttendanceRecordResponse;
import com.attendance.backend.dto.response.AttendanceSummaryResponse;
import com.attendance.backend.dto.response.ClassResponse;
import com.attendance.backend.service.AttendanceService;
import com.attendance.backend.service.EnrollmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final EnrollmentService enrollmentService;
    private final AttendanceService attendanceService;

    public StudentController(
            EnrollmentService enrollmentService,
            AttendanceService attendanceService
    ) {
        this.enrollmentService = enrollmentService;
        this.attendanceService = attendanceService;
    }

    @GetMapping("/classes")
    public ResponseEntity<List<ClassResponse>> getMyClasses(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<ClassResponse> response = enrollmentService.getEnrolledClassesForStudent(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/classes/{classId}/attendance")
    public ResponseEntity<List<AttendanceRecordResponse>> getClassAttendance(
            @PathVariable Long classId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<AttendanceRecordResponse> response = attendanceService.getStudentClassAttendance(classId, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/classes/{classId}/summary")
    public ResponseEntity<AttendanceSummaryResponse> getClassSummary(
            @PathVariable Long classId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        AttendanceSummaryResponse response = attendanceService.getStudentClassSummary(classId, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/attendance")
    public ResponseEntity<List<AttendanceRecordResponse>> getAllMyAttendance(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<AttendanceRecordResponse> response = attendanceService.getStudentAllAttendance(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/summaries")
    public ResponseEntity<List<AttendanceSummaryResponse>> getAllSummaries(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<AttendanceSummaryResponse> response = attendanceService.getStudentAllSummaries(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
}
