package com.myproject.video.video_platform.entity.products.course;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * A module or “section” inside a Course.
 */
@Entity
@Table(name = "course_sections")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CourseSection {

    @Id @GeneratedValue
    private UUID id;

    private String title;

    private String description;

    private Integer position;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private CourseProduct course;

    @OneToMany(
            mappedBy = "section",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("position ASC")
    private Set<com.myproject.video.video_platform.entity.products.course_product.CourseLesson> lessons = new LinkedHashSet<>();
}
