package com.myproject.video.video_platform.dto.products.course.quiz;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "Quiz payload used for editing and returning quiz definitions.")
public class QuizDraftDto {

    @Schema(description = "Quiz identifier", example = "cb36f14e-d6f6-4f60-a85f-204b9fc39a6c")
    private String id;

    @NotBlank
    @Schema(description = "Quiz title", example = "Module wrap-up assessment")
    private String title;

    @Schema(description = "Optional description shown in editors")
    private String description;

    @Schema(description = "Passing score threshold in percent", example = "70")
    private Integer passingScore;

    @Valid
    @NotEmpty
    @Size(min = 1)
    @Schema(description = "Collection of quiz questions")
    private List<QuizQuestionDto> questions = new ArrayList<>();
}
