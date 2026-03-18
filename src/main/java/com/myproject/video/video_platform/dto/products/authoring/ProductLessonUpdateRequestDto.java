package com.myproject.video.video_platform.dto.products.authoring;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Canonical request to patch a lesson inside a course section.")
public class ProductLessonUpdateRequestDto {

    @Schema(description = "Lesson title", example = "Introduction")
    private String title;

    @Schema(description = "Lesson type", example = "VIDEO")
    private String type;

    @Schema(description = "Video URL when type is VIDEO", example = "https://cdn.example.com/video.mp4")
    private String videoUrl;

    @Schema(description = "Rich text content when type is ARTICLE", example = "<p>Hello</p>")
    private String content;

    @Schema(description = "Lesson description", example = "Short summary shown in the editor.")
    private String description;

    @Schema(description = "Desired position inside the section", example = "1")
    private Integer position;
}
