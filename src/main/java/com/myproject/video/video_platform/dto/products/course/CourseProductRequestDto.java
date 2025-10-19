package com.myproject.video.video_platform.dto.products.course;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.myproject.video.video_platform.dto.products.AbstractProductRequestDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonTypeName("COURSE")
@Schema(description = "Payload for creating or updating a course product.")
public class CourseProductRequestDto extends AbstractProductRequestDto {
    @Schema(description = "Existing sections to sync with the course; omit to keep current layout")
    private List<CourseSectionResponseDto> sections;
}
