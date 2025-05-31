package com.myproject.video.video_platform.repository.products.course;

import com.myproject.video.video_platform.entity.auth.User;
import com.myproject.video.video_platform.entity.products.course.CourseProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseProductRepository extends JpaRepository<CourseProduct, UUID> {

    @Query("""
      SELECT DISTINCT cp
      FROM   CourseProduct cp
      LEFT   JOIN FETCH cp.sections sec
      LEFT   JOIN FETCH sec.lessons
      WHERE  cp.id = :id
    """)
    Optional<CourseProduct> findFullById(@Param("id") UUID id);

    List<CourseProduct> findAllByUser(User user);
}
