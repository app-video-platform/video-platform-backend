package com.myproject.video.video_platform.dto.products.course;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Command to append a new lesson to an existing section.")
public class CourseLessonCreateRequestDto {
    @NotBlank
    @Schema(description = "Lesson title", example = "Frame Your Story for Video")
    private String title;
    @NotBlank
    @Schema(description = "Lesson type", example = "VIDEO")
    private String type;
    @NotBlank
    @Schema(description = "Target section identifier", example = "c84b79a6-9d4d-4bb4-8f3c-3eb8bb76390a")
    private String sectionId;
    @NotBlank
    @Schema(description = "Owner user identifier", example = "738297f1-45fb-4f5f-98a5-6d0eb0a8f542")
    private String userId;
    @Schema(description = "Video asset URL", example = "https://cdn.serious-debauchery.click/videos/intro-storyboard.mp4")
    private String videoUrl;
    @Schema(description = "Rich text content when the lesson is ARTICLE", example = "<p>Outline your talking points...</p>")
    private String content;
    @Schema(description = "Optional explicit ordering", example = "1")
    private Integer position;
}
