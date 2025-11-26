package com.myproject.video.video_platform.dto.products.course.quiz;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "Response body describing computed scores for a quiz submission")
public class QuizSubmissionResponse {

    @Schema(description = "Lesson identifier")
    private String lessonId;

    @Schema(description = "Quiz identifier")
    private String quizId;

    @Schema(description = "Total points possible across the quiz")
    private Integer totalPointsPossible;

    @Schema(description = "Total points achieved in this attempt")
    private Integer totalPointsAchieved;

    @Schema(description = "Percentage score (0-100)")
    private BigDecimal percentage;

    @Schema(description = "Indicates if the learner passed the quiz")
    private boolean passed;

    @Schema(description = "Passing score threshold applied")
    private Integer passingScore;

    @Schema(description = "Per-question outcomes")
    private List<QuizQuestionResultDto> questions = new ArrayList<>();
}
