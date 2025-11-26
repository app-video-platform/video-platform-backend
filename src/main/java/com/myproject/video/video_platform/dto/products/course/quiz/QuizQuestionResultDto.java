package com.myproject.video.video_platform.dto.products.course.quiz;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Result details for a question inside a learner submission response")
public class QuizQuestionResultDto {

    @Schema(description = "Question identifier")
    private String questionId;

    @Schema(description = "Whether the learner answered correctly")
    private boolean correct;

    @Schema(description = "Points earned on this question")
    private Integer pointsAwarded;

    @Schema(description = "Optional explanation to show after submission")
    private String explanation;
}
