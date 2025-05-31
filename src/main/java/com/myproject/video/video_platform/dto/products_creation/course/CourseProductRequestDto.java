package com.myproject.video.video_platform.dto.products_creation.course;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.myproject.video.video_platform.dto.products_creation.AbstractProductRequestDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonTypeName("COURSE")
public class CourseProductRequestDto extends AbstractProductRequestDto {

}
