package com.myproject.video.video_platform.dto.products.course;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CourseLessonCreateRequestDto {
    @NotBlank
    private String title;
    @NotBlank
    private String type;
    @NotBlank
    private String sectionId;
    @NotBlank
    private String userId;
    private String videoUrl;
    private String content;
    private Integer position;
}
