package com.myproject.video.video_platform.repository.products.course.quiz;

import com.myproject.video.video_platform.entity.products.course.quiz.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, UUID> {
    Optional<Quiz> findByLessonId(UUID lessonId);
    boolean existsByLessonId(UUID lessonId);
}
