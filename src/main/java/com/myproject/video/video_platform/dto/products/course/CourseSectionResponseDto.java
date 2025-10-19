package com.myproject.video.video_platform.dto.products.course;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Course module containing ordered lessons.")
public class CourseSectionResponseDto {
    @Schema(description = "Section identifier", example = "c84b79a6-9d4d-4bb4-8f3c-3eb8bb76390a")
    private String id;
    @Schema(description = "Section title", example = "Planning Your Production Day")
    private String title;
    @Schema(description = "Narrative of what learners will achieve", example = "Assess filming goals, timelines, and deliverables before recording.")
    private String description;
    @Schema(description = "Owning product identifier", example = "a8c5d4a9-dc93-4c71-9c33-5d56f3d6b21d")
    private String productId;
    @Schema(description = "Relative ordering (starting at 1)", example = "2")
    private Integer position;
    @Schema(description = "Lessons contained in the section")
    private List<CourseLessonResponseDto> lessons;
}
