package com.attendance.backend.exception;

public class DuplicateAttendanceSessionException extends RuntimeException {
    public DuplicateAttendanceSessionException(String message) {
        super(message);
    }
}
