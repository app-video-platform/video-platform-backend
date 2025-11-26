package com.myproject.video.video_platform.dto.products.course.quiz;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Choice option surfaced inside quiz questions.")
public class QuizOptionDto {

    @Schema(description = "Option identifier", example = "08dabc86-4b00-4687-8882-cd54a398bf2d")
    private String id;

    @Schema(description = "Option text", example = "Frame the subject using rule of thirds")
    private String text;

    @Schema(description = "Marks the option as correct in authoring mode")
    private Boolean isCorrect;

    @Schema(description = "Relative ordering")
    private Integer sortOrder;
}
