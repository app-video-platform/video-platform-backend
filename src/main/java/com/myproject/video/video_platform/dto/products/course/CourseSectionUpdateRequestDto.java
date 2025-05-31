package com.myproject.video.video_platform.dto.products.course;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CourseSectionUpdateRequestDto {
    @NotBlank
    private String id;

    @NotBlank
    private String title;

    private Integer position;
}
