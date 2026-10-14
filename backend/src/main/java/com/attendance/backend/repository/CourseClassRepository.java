package com.attendance.backend.repository;

import com.attendance.backend.entity.CourseClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseClassRepository extends JpaRepository<CourseClass, Long> {

    List<CourseClass> findByTeacherIdOrderByCreatedAtDesc(Long teacherId);

    Optional<CourseClass> findByIdAndTeacherId(Long id, Long teacherId);

    Optional<CourseClass> findByClassCode(String classCode);

    boolean existsByClassCode(String classCode);
}
