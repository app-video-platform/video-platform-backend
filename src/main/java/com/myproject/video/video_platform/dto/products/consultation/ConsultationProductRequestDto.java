package com.myproject.video.video_platform.dto.products.consultation;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.myproject.video.video_platform.dto.products.AbstractProductRequestDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonTypeName("CONSULTATION")
@Schema(description = "Payload for defining one-to-one consultation slots.")
public class ConsultationProductRequestDto extends AbstractProductRequestDto {

    @Schema(description = "Consultation-specific fields nested under product.details")
    private ConsultationProductDetailsDto details;
}
