package com.myproject.video.video_platform.repository.products.course;

import com.myproject.video.video_platform.entity.products.course.CourseSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CourseSectionRepository extends JpaRepository<CourseSection, UUID> {
}
