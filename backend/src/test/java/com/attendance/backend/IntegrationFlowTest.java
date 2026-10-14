package com.attendance.backend;

import com.attendance.backend.dto.request.*;
import com.attendance.backend.dto.response.AuthResponse;
import com.attendance.backend.dto.response.ClassResponse;
import com.attendance.backend.entity.AttendanceStatus;
import com.attendance.backend.entity.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class IntegrationFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String teacherToken;
    private static String studentToken;
    private static Long createdClassId;
    private static Long studentUserId;
    private static Long createdSessionId;

    @Test
    @Order(1)
    void testRegisterTeacher() throws Exception {
        RegisterRequest req = new RegisterRequest("Prof Alan Turing", "turing@cs.edu", "securePass123", Role.TEACHER);

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.user.email").value("turing@cs.edu"))
                .andExpect(jsonPath("$.user.role").value("TEACHER"))
                .andReturn();

        AuthResponse authRes = objectMapper.readValue(result.getResponse().getContentAsString(), AuthResponse.class);
        teacherToken = authRes.getToken();
    }

    @Test
    @Order(2)
    void testRegisterStudent() throws Exception {
        RegisterRequest req = new RegisterRequest("Ada Lovelace", "ada@cs.edu", "securePass123", Role.STUDENT);

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.user.email").value("ada@cs.edu"))
                .andExpect(jsonPath("$.user.role").value("STUDENT"))
                .andReturn();

        AuthResponse authRes = objectMapper.readValue(result.getResponse().getContentAsString(), AuthResponse.class);
        studentToken = authRes.getToken();
        studentUserId = authRes.getUser().getId();
    }

    @Test
    @Order(3)
    void testDuplicateRegistrationFails() throws Exception {
        RegisterRequest req = new RegisterRequest("Duplicate Ada", "ada@cs.edu", "password123", Role.STUDENT);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(4)
    void testLoginTeacher() throws Exception {
        LoginRequest req = new LoginRequest("turing@cs.edu", "securePass123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.user.email").value("turing@cs.edu"));
    }

    @Test
    @Order(5)
    void testTeacherCreatesClass() throws Exception {
        CreateClassRequest req = new CreateClassRequest("Algorithms 101", "Computer Science", "Core algorithmic concepts");

        MvcResult result = mockMvc.perform(post("/api/teacher/classes")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.className").value("Algorithms 101"))
                .andExpect(jsonPath("$.classCode").isString())
                .andReturn();

        ClassResponse classRes = objectMapper.readValue(result.getResponse().getContentAsString(), ClassResponse.class);
        createdClassId = classRes.getId();
    }

    @Test
    @Order(6)
    void testStudentCannotCreateClass() throws Exception {
        CreateClassRequest req = new CreateClassRequest("Hacking 101", "Computer Science", "Unauthorized class");

        mockMvc.perform(post("/api/teacher/classes")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(7)
    void testTeacherEnrollsStudent() throws Exception {
        EnrollStudentRequest req = new EnrollStudentRequest("ada@cs.edu");

        mockMvc.perform(post("/api/teacher/classes/" + createdClassId + "/students")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.studentEmail").value("ada@cs.edu"))
                .andExpect(jsonPath("$.classId").value(createdClassId));
    }

    @Test
    @Order(8)
    void testDuplicateEnrollmentFails() throws Exception {
        EnrollStudentRequest req = new EnrollStudentRequest("ada@cs.edu");

        mockMvc.perform(post("/api/teacher/classes/" + createdClassId + "/students")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(9)
    void testTeacherCreatesAttendanceSession() throws Exception {
        CreateAttendanceSessionRequest req = new CreateAttendanceSessionRequest(LocalDate.of(2026, 10, 13));

        MvcResult result = mockMvc.perform(post("/api/teacher/classes/" + createdClassId + "/attendance-sessions")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.attendanceDate").value("2026-10-13"))
                .andReturn();

        createdSessionId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test
    @Order(10)
    void testDuplicateSessionOnSameDateFails() throws Exception {
        CreateAttendanceSessionRequest req = new CreateAttendanceSessionRequest(LocalDate.of(2026, 10, 13));

        mockMvc.perform(post("/api/teacher/classes/" + createdClassId + "/attendance-sessions")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(11)
    void testTeacherMarksAttendance() throws Exception {
        MarkAttendanceRequest req = new MarkAttendanceRequest(List.of(
                new AttendanceRecordRequest(studentUserId, AttendanceStatus.PRESENT)
        ));

        mockMvc.perform(put("/api/teacher/attendance-sessions/" + createdSessionId + "/records")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.presentCount").value(1))
                .andExpect(jsonPath("$.absentCount").value(0))
                .andExpect(jsonPath("$.records[0].status").value("PRESENT"));
    }

    @Test
    @Order(12)
    void testStudentViewsAttendanceAndSummary() throws Exception {
        mockMvc.perform(get("/api/student/classes/" + createdClassId + "/summary")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalClasses").value(1))
                .andExpect(jsonPath("$.presentCount").value(1))
                .andExpect(jsonPath("$.absentCount").value(0))
                .andExpect(jsonPath("$.attendancePercentage").value(100.0));
    }
}
