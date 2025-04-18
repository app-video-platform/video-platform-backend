package com.myproject.video.video_platform.dto.products_creation.consultation;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.myproject.video.video_platform.dto.products_creation.AbstractProductRequestDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonTypeName("CONSULTATION")
public class ConsultationProductRequestDto extends AbstractProductRequestDto {
}
