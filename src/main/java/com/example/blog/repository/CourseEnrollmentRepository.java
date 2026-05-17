package com.example.blog.repository;

import com.example.blog.entity.CourseEnrollment;
import com.example.blog.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Long> {
    List<CourseEnrollment> findByUser(User user);
    List<CourseEnrollment> findByUserAndStatus(User user, CourseEnrollment.EnrollmentStatus status);
}
