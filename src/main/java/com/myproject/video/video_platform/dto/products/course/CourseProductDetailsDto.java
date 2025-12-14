package com.myproject.video.video_platform.dto.products.course;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Course-specific fields nested under product.details.")
public class CourseProductDetailsDto {

    @Schema(description = "Ordered sections attached to the course")
    private List<CourseSectionResponseDto> sections;
}

