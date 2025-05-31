package com.myproject.video.video_platform.entity.products.course;

import com.myproject.video.video_platform.common.enums.products.course.LessonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A “lesson” inside a section (video, article, etc.).
 */
@Entity
@Table(name = "course_lessons")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CourseLesson {

    @Id @GeneratedValue
    private UUID id;

    private String title;

    @Enumerated(EnumType.STRING)
    private LessonType type;

    // If type=VIDEO, FE stores a CDN URL (String).
    private String videoUrl;

    // If type=ARTICLE, FE can send HTML/markdown content.
    @Column(columnDefinition = "TEXT")
    private String content;

    private Integer position;

    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "section_id", nullable = false)
    private CourseSection section;
}
