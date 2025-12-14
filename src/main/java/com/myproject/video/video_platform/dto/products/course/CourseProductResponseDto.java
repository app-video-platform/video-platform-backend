package com.myproject.video.video_platform.dto.products.course;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.myproject.video.video_platform.dto.products.AbstractProductResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonTypeName("COURSE")
@Schema(description = "Course product view with details nested under product.details.")
public class CourseProductResponseDto extends AbstractProductResponseDto {

    @Schema(description = "Course-specific fields nested under product.details")
    private CourseProductDetailsDto details;
}
