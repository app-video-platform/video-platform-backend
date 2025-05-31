package com.myproject.video.video_platform.dto.products.course;

import lombok.Data;

import java.util.UUID;

@Data
public class CourseLessonResponseDto {
    private UUID id;
    private String title;
    private String type;
    private String videoUrl;
    private String content;
    private Integer position;
}
