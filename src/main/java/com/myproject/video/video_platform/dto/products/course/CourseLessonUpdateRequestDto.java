package com.myproject.video.video_platform.dto.products.course;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CourseLessonUpdateRequestDto {
    @NotBlank
    private String id;
    @NotBlank
    private String type;
    @NotBlank
    private String userId;
    private String title;
    private String videoUrl;
    private String content;
    private Integer position;
}
