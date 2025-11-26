package com.myproject.video.video_platform.entity.products.course.quiz;

import com.myproject.video.video_platform.common.enums.products.course.QuizQuestionType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "quiz_questions")
@Getter
@Setter
public class QuizQuestion {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuizQuestionType type;

    @Column(nullable = false)
    private Integer points;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuizOption> options = new ArrayList<>();

    public void addOption(QuizOption option) {
        options.add(option);
        option.setQuestion(this);
    }

    public void removeOption(QuizOption option) {
        options.remove(option);
        option.setQuestion(null);
    }
}
