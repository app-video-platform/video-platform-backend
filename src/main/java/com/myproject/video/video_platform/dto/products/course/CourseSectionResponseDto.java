package com.myproject.video.video_platform.dto.products.course;

import lombok.Data;

import java.util.List;

@Data
public class CourseSectionResponseDto {
    private String id;
    private String title;
    private String description;
    private String productId;
    private Integer position;
    private List<CourseLessonResponseDto> lessons;
}
