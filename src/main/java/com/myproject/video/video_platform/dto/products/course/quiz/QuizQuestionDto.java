package com.myproject.video.video_platform.dto.products.course.quiz;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "Question definition used for both authoring and play mode")
public class QuizQuestionDto {

    @Schema(description = "Question identifier", example = "de598c74-3b1d-420e-8cc4-278578d442de")
    private String id;

    @NotBlank
    @Schema(description = "Question title", example = "Pick the correct statements")
    private String title;

    @NotBlank
    @Schema(description = "Question type", allowableValues = {"multiple_choice_single", "multiple_choice_multi", "true_false"})
    private String type;

    @Positive
    @Schema(description = "Full points awarded upon correctness", example = "5")
    private Integer points;

    @Schema(description = "Optional explanation surfaced after submission")
    private String explanation;

    @NotNull
    @Schema(description = "Answer options")
    private List<QuizOptionDto> options = new ArrayList<>();

    @Schema(description = "Relative ordering")
    private Integer position;
}
