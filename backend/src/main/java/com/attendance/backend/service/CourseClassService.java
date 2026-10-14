package com.attendance.backend.service;

import com.attendance.backend.dto.request.CreateClassRequest;
import com.attendance.backend.dto.request.UpdateClassRequest;
import com.attendance.backend.dto.response.ClassResponse;
import com.attendance.backend.entity.CourseClass;
import com.attendance.backend.entity.Role;
import com.attendance.backend.entity.User;
import com.attendance.backend.exception.ResourceNotFoundException;
import com.attendance.backend.exception.UnauthorizedAccessException;
import com.attendance.backend.repository.AttendanceSessionRepository;
import com.attendance.backend.repository.CourseClassRepository;
import com.attendance.backend.repository.EnrollmentRepository;
import com.attendance.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CourseClassService {

    private final CourseClassRepository courseClassRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AttendanceSessionRepository attendanceSessionRepository;

    public CourseClassService(
            CourseClassRepository courseClassRepository,
            UserRepository userRepository,
            EnrollmentRepository enrollmentRepository,
            AttendanceSessionRepository attendanceSessionRepository
    ) {
        this.courseClassRepository = courseClassRepository;
        this.userRepository = userRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.attendanceSessionRepository = attendanceSessionRepository;
    }

    private User getTeacherByEmail(String email) {
        User teacher = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        if (teacher.getRole() != Role.TEACHER) {
            throw new UnauthorizedAccessException("Only users with role TEACHER can perform this action");
        }
        return teacher;
    }

    private String generateUniqueClassCode() {
        String code;
        do {
            code = "CLS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (courseClassRepository.existsByClassCode(code));
        return code;
    }

    @Transactional
    public ClassResponse createClass(CreateClassRequest request, String teacherEmail) {
        User teacher = getTeacherByEmail(teacherEmail);
        String classCode = generateUniqueClassCode();

        CourseClass courseClass = new CourseClass(
                request.getClassName().trim(),
                request.getSubjectName().trim(),
                request.getDescription() != null ? request.getDescription().trim() : null,
                classCode,
                teacher
        );

        CourseClass saved = courseClassRepository.save(courseClass);
        return ClassResponse.fromEntity(saved, 0, 0);
    }

    @Transactional(readOnly = true)
    public List<ClassResponse> getTeacherClasses(String teacherEmail) {
        User teacher = getTeacherByEmail(teacherEmail);
        List<CourseClass> classes = courseClassRepository.findByTeacherIdOrderByCreatedAtDesc(teacher.getId());

        return classes.stream().map(c -> {
            long studentCount = enrollmentRepository.countByCourseClassId(c.getId());
            long sessionCount = attendanceSessionRepository.countByCourseClassId(c.getId());
            return ClassResponse.fromEntity(c, studentCount, sessionCount);
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ClassResponse getClassById(Long classId, String teacherEmail) {
        User teacher = getTeacherByEmail(teacherEmail);
        CourseClass courseClass = courseClassRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + classId));

        if (!courseClass.getTeacher().getId().equals(teacher.getId())) {
            throw new UnauthorizedAccessException("You can only access classes created by you");
        }

        long studentCount = enrollmentRepository.countByCourseClassId(courseClass.getId());
        long sessionCount = attendanceSessionRepository.countByCourseClassId(courseClass.getId());
        return ClassResponse.fromEntity(courseClass, studentCount, sessionCount);
    }

    @Transactional
    public ClassResponse updateClass(Long classId, UpdateClassRequest request, String teacherEmail) {
        User teacher = getTeacherByEmail(teacherEmail);
        CourseClass courseClass = courseClassRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + classId));

        if (!courseClass.getTeacher().getId().equals(teacher.getId())) {
            throw new UnauthorizedAccessException("You can only update classes created by you");
        }

        courseClass.setClassName(request.getClassName().trim());
        courseClass.setSubjectName(request.getSubjectName().trim());
        courseClass.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);

        CourseClass updated = courseClassRepository.save(courseClass);
        long studentCount = enrollmentRepository.countByCourseClassId(updated.getId());
        long sessionCount = attendanceSessionRepository.countByCourseClassId(updated.getId());
        return ClassResponse.fromEntity(updated, studentCount, sessionCount);
    }

    @Transactional
    public void deleteClass(Long classId, String teacherEmail) {
        User teacher = getTeacherByEmail(teacherEmail);
        CourseClass courseClass = courseClassRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + classId));

        if (!courseClass.getTeacher().getId().equals(teacher.getId())) {
            throw new UnauthorizedAccessException("You can only delete classes created by you");
        }

        courseClassRepository.delete(courseClass);
    }

    @Transactional(readOnly = true)
    public CourseClass getVerifiedClassForTeacher(Long classId, String teacherEmail) {
        User teacher = getTeacherByEmail(teacherEmail);
        CourseClass courseClass = courseClassRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + classId));

        if (!courseClass.getTeacher().getId().equals(teacher.getId())) {
            throw new UnauthorizedAccessException("You can only manage classes created by you");
        }
        return courseClass;
    }
}
