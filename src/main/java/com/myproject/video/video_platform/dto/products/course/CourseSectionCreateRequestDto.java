package com.myproject.video.video_platform.dto.products.course;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Command to add a new section to a course.")
public class CourseSectionCreateRequestDto {
    @NotBlank(message = "Title is required")
    @Schema(description = "Section title", example = "Planning Your Production Day")
    private String title;
    @NotBlank(message = "CourseId is required")
    @Schema(description = "Parent course identifier", example = "a8c5d4a9-dc93-4c71-9c33-5d56f3d6b21d")
    private String productId;
    @NotBlank(message = "User ID is required")
    @Schema(description = "Owner user identifier", example = "738297f1-45fb-4f5f-98a5-6d0eb0a8f542")
    private String userId;
    @Schema(description = "Optional description", example = "Blueprint for structuring your shoot day")
    private String description;
    @Schema(description = "Desired position within the course", example = "2")
    private Integer position;
}
