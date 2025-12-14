package com.myproject.video.video_platform.dto.products.consultation;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.myproject.video.video_platform.dto.products.AbstractProductResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonTypeName("CONSULTATION")
@Schema(description = "Consultation product view with details nested under product.details.")
public class ConsultationProductResponseDto extends AbstractProductResponseDto {

    @Schema(description = "Consultation-specific fields nested under product.details")
    private ConsultationProductDetailsDto details;
}
