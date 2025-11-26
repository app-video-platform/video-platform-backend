package com.myproject.video.video_platform.dto.products.course.quiz;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "Answer payload for a specific quiz question")
public class QuizSubmissionAnswerDto {

    @NotBlank
    @Schema(description = "Question identifier", example = "de598c74-3b1d-420e-8cc4-278578d442de")
    private String questionId;

    @Schema(description = "Selected option identifiers", example = "['08dabc86-4b00-4687-8882-cd54a398bf2d']")
    private List<String> selectedOptionIds = new ArrayList<>();
}
