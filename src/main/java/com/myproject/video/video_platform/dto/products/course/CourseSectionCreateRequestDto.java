package com.myproject.video.video_platform.dto.products.course;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CourseSectionCreateRequestDto {
    @NotBlank(message = "Title is required")
    private String title;
    @NotBlank(message = "CourseId is required")
    private String productId;
    @NotBlank(message = "User ID is required")
    private String userId;
    private String description;
    private Integer position;
}
