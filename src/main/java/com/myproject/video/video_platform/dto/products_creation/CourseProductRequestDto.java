package com.myproject.video.video_platform.dto.products_creation;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonTypeName("COURSE")
public class CourseProductRequestDto extends AbstractProductRequestDto {
}
