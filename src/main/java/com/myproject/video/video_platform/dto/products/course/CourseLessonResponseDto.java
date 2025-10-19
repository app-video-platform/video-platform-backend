package com.myproject.video.video_platform.dto.products.course;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Read-only lesson summary surfaced in course views.")
public class CourseLessonResponseDto {
    @Schema(description = "Lesson identifier", example = "d241f59a-9493-4e72-8544-3d02f4c3e5be")
    private String id;
    @Schema(description = "Lesson title", example = "Frame Your Story for Video")
    private String title;
    @Schema(description = "Lesson type", example = "VIDEO")
    private String type;
    @Schema(description = "Video asset CDN URL", example = "https://cdn.serious-debauchery.click/videos/intro-storyboard.mp4")
    private String videoUrl;
    @Schema(description = "Lesson article content when type is ARTICLE", example = "<p>Outline your talking points...</p>")
    private String content;
    @Schema(description = "Relative ordering inside the section", example = "1")
    private Integer position;
}
