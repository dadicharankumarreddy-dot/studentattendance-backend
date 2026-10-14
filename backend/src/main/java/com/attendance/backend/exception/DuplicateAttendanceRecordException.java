package com.attendance.backend.exception;

public class DuplicateAttendanceRecordException extends RuntimeException {
    public DuplicateAttendanceRecordException(String message) {
        super(message);
    }
}
