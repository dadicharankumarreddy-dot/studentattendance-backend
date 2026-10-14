package com.attendance.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class EnrollStudentRequest {

    @NotBlank(message = "Student email is required")
    @Email(message = "Please provide a valid student email address")
    private String studentEmail;

    public EnrollStudentRequest() {
    }

    public EnrollStudentRequest(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }
}
