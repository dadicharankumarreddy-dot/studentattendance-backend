package com.attendance.backend;

import com.attendance.backend.dto.request.CreateClassRequest;
import com.attendance.backend.dto.request.EnrollStudentRequest;
import com.attendance.backend.dto.response.ClassResponse;
import com.attendance.backend.dto.response.EnrollmentResponse;
import com.attendance.backend.entity.CourseClass;
import com.attendance.backend.entity.Enrollment;
import com.attendance.backend.entity.Role;
import com.attendance.backend.entity.User;
import com.attendance.backend.exception.BadRequestException;
import com.attendance.backend.exception.DuplicateEnrollmentException;
import com.attendance.backend.exception.UnauthorizedAccessException;
import com.attendance.backend.repository.AttendanceSessionRepository;
import com.attendance.backend.repository.CourseClassRepository;
import com.attendance.backend.repository.EnrollmentRepository;
import com.attendance.backend.repository.UserRepository;
import com.attendance.backend.service.CourseClassService;
import com.attendance.backend.service.EnrollmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CourseClassAndEnrollmentServiceTest {

    @Mock
    private CourseClassRepository courseClassRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private AttendanceSessionRepository attendanceSessionRepository;

    private CourseClassService courseClassService;
    private EnrollmentService enrollmentService;

    private User teacher1;
    private User teacher2;
    private User student1;
    private CourseClass sampleClass;

    @BeforeEach
    void setUp() {
        courseClassService = new CourseClassService(
                courseClassRepository,
                userRepository,
                enrollmentRepository,
                attendanceSessionRepository
        );

        enrollmentService = new EnrollmentService(
                enrollmentRepository,
                courseClassService,
                userRepository,
                attendanceSessionRepository
        );

        teacher1 = new User("Teacher One", "t1@example.com", "pass", Role.TEACHER);
        teacher1.setId(10L);

        teacher2 = new User("Teacher Two", "t2@example.com", "pass", Role.TEACHER);
        teacher2.setId(20L);

        student1 = new User("Student One", "s1@example.com", "pass", Role.STUDENT);
        student1.setId(30L);

        sampleClass = new CourseClass("Data Structures", "CS101", "Core CS course", "CLS-ABC12345", teacher1);
        sampleClass.setId(100L);
    }

    @Test
    void testCreateClassAssignsTeacher() {
        CreateClassRequest req = new CreateClassRequest("Data Structures", "CS101", "Core CS course");

        when(userRepository.findByEmail("t1@example.com")).thenReturn(Optional.of(teacher1));
        when(courseClassRepository.existsByClassCode(anyString())).thenReturn(false);
        when(courseClassRepository.save(any(CourseClass.class))).thenReturn(sampleClass);

        ClassResponse res = courseClassService.createClass(req, "t1@example.com");

        assertNotNull(res);
        assertEquals("Data Structures", res.getClassName());
        assertEquals(teacher1.getId(), res.getTeacherId());
        verify(courseClassRepository, times(1)).save(any(CourseClass.class));
    }

    @Test
    void testTeacherCannotAccessAnotherTeachersClass() {
        when(userRepository.findByEmail("t2@example.com")).thenReturn(Optional.of(teacher2));
        when(courseClassRepository.findById(100L)).thenReturn(Optional.of(sampleClass));

        assertThrows(UnauthorizedAccessException.class, () ->
                courseClassService.getClassById(100L, "t2@example.com")
        );
    }

    @Test
    void testEnrollStudentSuccess() {
        EnrollStudentRequest req = new EnrollStudentRequest("s1@example.com");

        when(userRepository.findByEmail("t1@example.com")).thenReturn(Optional.of(teacher1));
        when(courseClassRepository.findById(100L)).thenReturn(Optional.of(sampleClass));
        when(userRepository.findByEmail("s1@example.com")).thenReturn(Optional.of(student1));
        when(enrollmentRepository.existsByCourseClassIdAndStudentId(100L, 30L)).thenReturn(false);

        Enrollment enrollment = new Enrollment(sampleClass, student1);
        enrollment.setId(500L);
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(enrollment);

        EnrollmentResponse res = enrollmentService.enrollStudent(100L, req, "t1@example.com");

        assertNotNull(res);
        assertEquals(30L, res.getStudentId());
        assertEquals(100L, res.getClassId());
        verify(enrollmentRepository, times(1)).save(any(Enrollment.class));
    }

    @Test
    void testEnrollStudentDuplicateThrowsException() {
        EnrollStudentRequest req = new EnrollStudentRequest("s1@example.com");

        when(userRepository.findByEmail("t1@example.com")).thenReturn(Optional.of(teacher1));
        when(courseClassRepository.findById(100L)).thenReturn(Optional.of(sampleClass));
        when(userRepository.findByEmail("s1@example.com")).thenReturn(Optional.of(student1));
        when(enrollmentRepository.existsByCourseClassIdAndStudentId(100L, 30L)).thenReturn(true);

        assertThrows(DuplicateEnrollmentException.class, () ->
                enrollmentService.enrollStudent(100L, req, "t1@example.com")
        );
        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    @Test
    void testEnrollTeacherAsStudentThrowsException() {
        EnrollStudentRequest req = new EnrollStudentRequest("t2@example.com");

        when(userRepository.findByEmail("t1@example.com")).thenReturn(Optional.of(teacher1));
        when(courseClassRepository.findById(100L)).thenReturn(Optional.of(sampleClass));
        when(userRepository.findByEmail("t2@example.com")).thenReturn(Optional.of(teacher2));

        assertThrows(BadRequestException.class, () ->
                enrollmentService.enrollStudent(100L, req, "t1@example.com")
        );
    }
}
