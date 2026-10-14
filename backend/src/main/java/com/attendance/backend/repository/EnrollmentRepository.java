package com.attendance.backend.repository;

import com.attendance.backend.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findByCourseClassIdOrderByEnrolledAtDesc(Long classId);

    List<Enrollment> findByStudentIdOrderByEnrolledAtDesc(Long studentId);

    Optional<Enrollment> findByCourseClassIdAndStudentId(Long classId, Long studentId);

    boolean existsByCourseClassIdAndStudentId(Long classId, Long studentId);

    void deleteByCourseClassIdAndStudentId(Long classId, Long studentId);

    long countByCourseClassId(Long classId);
}
