package com.myproject.video.video_platform.entity.products.course.quiz;

import com.myproject.video.video_platform.entity.products.course.CourseLesson;
import com.myproject.video.video_platform.entity.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "quiz_attempts")
@Getter
@Setter
public class QuizAttempt {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private CourseLesson lesson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @Column(name = "points_possible", nullable = false)
    private Integer totalPointsPossible;

    @Column(name = "points_achieved", nullable = false)
    private Integer totalPointsAchieved;

    @Column(name = "percentage", precision = 5, scale = 1, nullable = false)
    private BigDecimal percentage;

    @Column(name = "passed", nullable = false)
    private boolean passed;

    @Column(name = "answers_json", columnDefinition = "TEXT", nullable = false)
    private String answersJson;
}
