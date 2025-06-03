package com.myproject.video.video_platform.dto.products.course;

import lombok.Data;

@Data
public class CourseLessonResponseDto {
    private String id;
    private String title;
    private String type;
    private String videoUrl;
    private String content;
    private Integer position;
}
