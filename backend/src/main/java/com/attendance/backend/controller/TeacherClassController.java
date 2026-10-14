package com.attendance.backend.controller;

import com.attendance.backend.dto.request.CreateAttendanceSessionRequest;
import com.attendance.backend.dto.request.CreateClassRequest;
import com.attendance.backend.dto.request.EnrollStudentRequest;
import com.attendance.backend.dto.request.UpdateClassRequest;
import com.attendance.backend.dto.response.AttendanceSessionResponse;
import com.attendance.backend.dto.response.ClassResponse;
import com.attendance.backend.dto.response.EnrollmentResponse;
import com.attendance.backend.service.AttendanceService;
import com.attendance.backend.service.CourseClassService;
import com.attendance.backend.service.EnrollmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teacher/classes")
public class TeacherClassController {

    private final CourseClassService courseClassService;
    private final EnrollmentService enrollmentService;
    private final AttendanceService attendanceService;

    public TeacherClassController(
            CourseClassService courseClassService,
            EnrollmentService enrollmentService,
            AttendanceService attendanceService
    ) {
        this.courseClassService = courseClassService;
        this.enrollmentService = enrollmentService;
        this.attendanceService = attendanceService;
    }

    @PostMapping
    public ResponseEntity<ClassResponse> createClass(
            @Valid @RequestBody CreateClassRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        ClassResponse response = courseClassService.createClass(request, userDetails.getUsername());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ClassResponse>> getMyClasses(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<ClassResponse> response = courseClassService.getTeacherClasses(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{classId}")
    public ResponseEntity<ClassResponse> getClassById(
            @PathVariable Long classId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        ClassResponse response = courseClassService.getClassById(classId, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{classId}")
    public ResponseEntity<ClassResponse> updateClass(
            @PathVariable Long classId,
            @Valid @RequestBody UpdateClassRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        ClassResponse response = courseClassService.updateClass(classId, request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{classId}")
    public ResponseEntity<Void> deleteClass(
            @PathVariable Long classId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        courseClassService.deleteClass(classId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    // Enrollment endpoints
    @PostMapping("/{classId}/students")
    public ResponseEntity<EnrollmentResponse> enrollStudent(
            @PathVariable Long classId,
            @Valid @RequestBody EnrollStudentRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        EnrollmentResponse response = enrollmentService.enrollStudent(classId, request, userDetails.getUsername());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{classId}/students")
    public ResponseEntity<List<EnrollmentResponse>> getEnrolledStudents(
            @PathVariable Long classId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<EnrollmentResponse> response = enrollmentService.getStudentsInClass(classId, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{classId}/students/{studentId}")
    public ResponseEntity<Void> removeStudent(
            @PathVariable Long classId,
            @PathVariable Long studentId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        enrollmentService.removeStudentFromClass(classId, studentId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    // Attendance session creation and listing
    @PostMapping("/{classId}/attendance-sessions")
    public ResponseEntity<AttendanceSessionResponse> createAttendanceSession(
            @PathVariable Long classId,
            @Valid @RequestBody CreateAttendanceSessionRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        AttendanceSessionResponse response = attendanceService.createSession(classId, request, userDetails.getUsername());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{classId}/attendance-sessions")
    public ResponseEntity<List<AttendanceSessionResponse>> getAttendanceSessions(
            @PathVariable Long classId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<AttendanceSessionResponse> response = attendanceService.getSessionsForClass(classId, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
}
