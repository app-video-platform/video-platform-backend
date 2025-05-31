package com.myproject.video.video_platform.dto.products.course;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CourseSectionCreateRequestDto {
    @NotBlank(message = "Title is required")
    private String title;

    private String description;
}
