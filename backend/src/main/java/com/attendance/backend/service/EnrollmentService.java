package com.attendance.backend.service;

import com.attendance.backend.dto.request.EnrollStudentRequest;
import com.attendance.backend.dto.response.ClassResponse;
import com.attendance.backend.dto.response.EnrollmentResponse;
import com.attendance.backend.entity.CourseClass;
import com.attendance.backend.entity.Enrollment;
import com.attendance.backend.entity.Role;
import com.attendance.backend.entity.User;
import com.attendance.backend.exception.BadRequestException;
import com.attendance.backend.exception.DuplicateEnrollmentException;
import com.attendance.backend.exception.ResourceNotFoundException;
import com.attendance.backend.repository.AttendanceSessionRepository;
import com.attendance.backend.repository.EnrollmentRepository;
import com.attendance.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseClassService courseClassService;
    private final UserRepository userRepository;
    private final AttendanceSessionRepository attendanceSessionRepository;

    public EnrollmentService(
            EnrollmentRepository enrollmentRepository,
            CourseClassService courseClassService,
            UserRepository userRepository,
            AttendanceSessionRepository attendanceSessionRepository
    ) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseClassService = courseClassService;
        this.userRepository = userRepository;
        this.attendanceSessionRepository = attendanceSessionRepository;
    }

    @Transactional
    public EnrollmentResponse enrollStudent(Long classId, EnrollStudentRequest request, String teacherEmail) {
        CourseClass courseClass = courseClassService.getVerifiedClassForTeacher(classId, teacherEmail);

        String studentEmail = request.getStudentEmail().trim().toLowerCase();
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("No registered student found with email: " + studentEmail));

        if (student.getRole() != Role.STUDENT) {
            throw new BadRequestException("User with email '" + studentEmail + "' is not a student (Role: " + student.getRole() + ")");
        }

        if (enrollmentRepository.existsByCourseClassIdAndStudentId(courseClass.getId(), student.getId())) {
            throw new DuplicateEnrollmentException("Student with email '" + studentEmail + "' is already enrolled in this class");
        }

        Enrollment enrollment = new Enrollment(courseClass, student);
        Enrollment saved = enrollmentRepository.save(enrollment);

        return EnrollmentResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getStudentsInClass(Long classId, String teacherEmail) {
        CourseClass courseClass = courseClassService.getVerifiedClassForTeacher(classId, teacherEmail);
        List<Enrollment> enrollments = enrollmentRepository.findByCourseClassIdOrderByEnrolledAtDesc(courseClass.getId());

        return enrollments.stream()
                .map(EnrollmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeStudentFromClass(Long classId, Long studentId, String teacherEmail) {
        CourseClass courseClass = courseClassService.getVerifiedClassForTeacher(classId, teacherEmail);

        Enrollment enrollment = enrollmentRepository.findByCourseClassIdAndStudentId(courseClass.getId(), studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student with id " + studentId + " is not enrolled in this class"));

        enrollmentRepository.delete(enrollment);
    }

    @Transactional(readOnly = true)
    public List<ClassResponse> getEnrolledClassesForStudent(String studentEmail) {
        User student = userRepository.findByEmail(studentEmail.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with email: " + studentEmail));

        List<Enrollment> enrollments = enrollmentRepository.findByStudentIdOrderByEnrolledAtDesc(student.getId());

        return enrollments.stream().map(enrollment -> {
            CourseClass courseClass = enrollment.getCourseClass();
            long studentCount = enrollmentRepository.countByCourseClassId(courseClass.getId());
            long sessionCount = attendanceSessionRepository.countByCourseClassId(courseClass.getId());
            return ClassResponse.fromEntity(courseClass, studentCount, sessionCount);
        }).collect(Collectors.toList());
    }
}
