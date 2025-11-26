package com.myproject.video.video_platform.repository.products.course.quiz;

import com.myproject.video.video_platform.entity.products.course.quiz.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, UUID> {
}
