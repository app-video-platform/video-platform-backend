package com.myproject.video.video_platform.dto.products.course;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CourseSectionResponseDto {
    private UUID id;
    private String title;
    private Integer position;
    private List<CourseLessonResponseDto> lessons;
}
