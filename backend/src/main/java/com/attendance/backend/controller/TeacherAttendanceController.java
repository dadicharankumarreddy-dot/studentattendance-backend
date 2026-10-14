package com.attendance.backend.controller;

import com.attendance.backend.dto.request.MarkAttendanceRequest;
import com.attendance.backend.dto.response.AttendanceSessionResponse;
import com.attendance.backend.service.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teacher/attendance-sessions")
public class TeacherAttendanceController {

    private final AttendanceService attendanceService;

    public TeacherAttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<AttendanceSessionResponse> getSessionDetails(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        AttendanceSessionResponse response = attendanceService.getSessionById(sessionId, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{sessionId}/records")
    public ResponseEntity<AttendanceSessionResponse> markOrUpdateAttendance(
            @PathVariable Long sessionId,
            @Valid @RequestBody MarkAttendanceRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        AttendanceSessionResponse response = attendanceService.markOrUpdateAttendance(sessionId, request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
}
