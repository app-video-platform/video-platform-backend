package com.myproject.video.video_platform.dto.products.authoring;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Canonical request to create a lesson inside a course section.")
public class ProductLessonCreateRequestDto {

    @NotBlank(message = "Title is required")
    @Schema(description = "Lesson title", example = "Introduction")
    private String title;

    @NotBlank(message = "Type is required")
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
