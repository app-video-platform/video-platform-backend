package com.myproject.video.video_platform.dto.products.course;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.myproject.video.video_platform.dto.products.AbstractProductResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonTypeName("COURSE")
@Schema(description = "Course product view including high-level section summaries.")
public class CourseProductResponseDto extends AbstractProductResponseDto {
    @Schema(description = "Ordered sections attached to the course")
    private List<CourseSectionResponseDto> sections;
}
