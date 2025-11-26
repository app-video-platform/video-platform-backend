package com.myproject.video.video_platform.dto.products.course.quiz;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "Submission payload containing learner answers for a quiz attempt.")
public class QuizSubmissionRequest {

    @Valid
    @Schema(description = "List of question answers")
    private List<QuizSubmissionAnswerDto> answers = new ArrayList<>();
}
