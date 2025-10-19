package com.myproject.video.video_platform.dto.products.course;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Command to update lesson metadata or content.")
public class CourseLessonUpdateRequestDto {
    @NotBlank
    @Schema(description = "Lesson identifier", example = "d241f59a-9493-4e72-8544-3d02f4c3e5be")
    private String id;
    @NotBlank
    @Schema(description = "Lesson type", example = "VIDEO")
    private String type;
    @NotBlank
    @Schema(description = "Owner user identifier", example = "738297f1-45fb-4f5f-98a5-6d0eb0a8f542")
    private String userId;
    @Schema(description = "Lesson title", example = "Frame Your Story for Video")
    private String title;
    @Schema(description = "Video asset URL", example = "https://cdn.serious-debauchery.click/videos/intro-storyboard.mp4")
    private String videoUrl;
    @Schema(description = "Lesson article content when applicable", example = "<p>Outline your talking points...</p>")
    private String content;
    @Schema(description = "Relative ordering inside the section", example = "1")
    private Integer position;
}
